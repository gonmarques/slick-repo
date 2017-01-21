/*
 * MIT License
 *
 * Copyright (c) 2016 Gonçalo Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.byteslounge.slickrepo.repository

import java.sql.Timestamp
import java.time.Instant

import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.version.VersionGenerator
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import com.byteslounge.slickrepo.scalaversion.RelationalProfile
import slick.jdbc.JdbcType

import scala.concurrent.ExecutionContext

/**
 * Repository used to execute CRUD operations against a database for
 * a given versioned entity type.
 */
abstract class VersionedRepository[T <: VersionedEntity[T, ID, V], ID, V : VersionGenerator] (override val driver: JdbcProfile) extends Repository[T, ID](driver) {

  import driver.api._

  def versionType: BaseTypedType[V]
  implicit lazy val _versionType: BaseTypedType[V] = versionType
  type TableType <: Versioned[ID, V] with RelationalProfile#Table[T]
  val generator: VersionGenerator[V] = implicitly[VersionGenerator[V]]

  /**
  * Persists an entity for the first time and also applies
  * an initial version to the entity.
  *
  * If the entity has an already assigned primary key, then it will
  * be persisted with that same primary key.
  *
  * If the entity doesn't have an already assigned primary key, then
  * it will be persisted using an auto-generated primary key using
  * the generation strategy configured in the entity definition.
  *
  * A new entity with both primary key and version assigned to it
  * will be returned.
  */
  override def save(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    entity.id match {
      case None    => saveUsingGeneratedId(entity)
      case Some(_) => saveUsingPredefinedId(entity)
    }
  }

  /**
  * Persists an entity with its initial version using an auto-generated primary key.
  */
  private def saveUsingGeneratedId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    (saveCompiled += versionedEntity).map(id => versionedEntity.withId(id))
  }

  /**
  * Persists an entity with its initial version using a predefined primary key.
  */
  private def saveUsingPredefinedId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    (tableQueryCompiled += versionedEntity).map(_ => versionedEntity)
  }

  /**
  * Updates a given entity in the database and also the entity
  * version field.
  *
  * If the entity is not yet persisted in the database then
  * this operation will result in an exception being thrown.
  *
  * Returns a new entity instance that has the version field
  * updated with the next version value.
  */
  override def update(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    findOneVersionedCompiled(versionedEntity.id.get, entity.version.get).update(versionedEntity)
      .map(updateCheck(versionedEntity, entity.version.get))
  }

  /**
  * Applies a new version or the next version to a given entity.
  *
  * If the entity has no version yet, then the initial version
  * will be assigned.
  *
  * If the entity already has an assigned version, then the next
  * version will be assigned.
  */
  private def applyVersion(entity: T): T = {
    entity.withVersion(entity.version.map(v => generator.nextVersion(v)).getOrElse(generator.initialVersion()))
  }

  /**
  * Checks the updated rows count for a given update statement that
  * was executed against a versioned entity record.
  *
  * If the record count is equal to 1 then the update succeeded. The
  * record with the expected version was found.
  *
  * If the record count is different than 1 then the update failed. The
  * record with the expected version was not found.
  */
  private def updateCheck(updatedEntity: T, expectedVersion: Any): (Int => T) = {
    (count) => {
      count match {
        case 1 => updatedEntity
        case _ => throw new OptimisticLockException("Failed to update entity of type " + updatedEntity.getClass.getName + ". Expected version was not found: " + expectedVersion)
      }
    }
  }

  lazy private val findOneVersionedCompiled = Compiled((id: Rep[ID], version: Rep[V]) => tableQuery.filter(_.id === id).filter(_.version === version))

  implicit val instantToSqlTimestampMapper: JdbcType[Instant] with BaseTypedType[Instant] = MappedColumnType.base[Instant, Timestamp](
    { instant => new java.sql.Timestamp(instant.toEpochMilli) },
    { sqlTimestamp => Instant.ofEpochMilli(sqlTimestamp.getTime) })
}
