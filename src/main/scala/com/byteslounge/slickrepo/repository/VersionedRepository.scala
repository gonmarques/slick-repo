/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.byteslounge.slickrepo.repository

import java.sql.Timestamp
import java.time.Instant

import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile
import slick.jdbc.JdbcType
import slick.profile.RelationalProfile

import scala.reflect.runtime.universe._
import scala.concurrent.ExecutionContext

/**
 * Repository used to execute CRUD operations against a database for
 * a given versioned entity type.
 */
abstract class VersionedRepository[T <: VersionedEntity[T, ID, V], ID, V : TypeTag] (override val driver: JdbcProfile) extends Repository[T, ID](driver) {

  import driver.api._

  def versionType: BaseTypedType[V]
  implicit lazy val _versionType: BaseTypedType[V] = versionType
  type TableType <: Versioned[ID, V] with RelationalProfile#Table[T]

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
    entity.withNewVersion(entity.version)
  }

  /**
  * Checks the updated rows count for a given update statement that
  * was eecuted against a versioned entity record.
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
