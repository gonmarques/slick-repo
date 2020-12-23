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
import java.time.{Instant, LocalDateTime}

import com.byteslounge.slickrepo.datetime.DateTimeHelper
import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.scalaversion.{JdbcProfile, RelationalProfile}
import com.byteslounge.slickrepo.version._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

import scala.concurrent.ExecutionContext

/**
 * Repository used to execute CRUD operations against a database for
 * a given versioned entity type.
 */
abstract class VersionedRepository[T <: VersionedEntity[T, ID, V], ID, V : VersionGenerator] extends Repository[T, ID] {

  import driver.api._

  def versionType: BaseTypedType[V]
  implicit lazy val _versionType: BaseTypedType[V] = versionType
  type TableType <: Versioned[ID, V] with RelationalProfile#Table[T]
  val generator: VersionGenerator[V] = implicitly[VersionGenerator[V]]

  /**
  * Versioned entity generated ID persister
  */
  override protected val generatedIdPersister: (T, ExecutionContext) => DBIO[T] =
    getGeneratedIdPersister(versionApplier)

  /**
  * Versioned entity predefined ID persister
  */
  override protected val predefinedIdPersister: (T, ExecutionContext) => DBIO[T] =
    getPredefinedIdPersister(versionApplier)

  /**
  * Batch persister
  */
  override protected val batchPersister: Seq[T] => DBIO[Option[Int]] =
    getBatchPersister(entities => entities.map(_getPrePersist compose versionApplier))

  /**
  * Updater
  */
  override protected val updater: (T, F, ExecutionContext) => DBIO[T] =
    getUpdater(versionApplier)

  /**
  * Update validator
  */
  override protected def updateValidator(previous: T, next: T): Int => T =
    updateCheck(next, previous.version.get)

  /**
  * Update finder
  */
  override protected def updateFinder(entity: T): F =
    findOneVersionedCompiled(entity.id.get, entity.version.get)

  /**
  * Applies a new version or the next version to a given entity.
  *
  * If the entity has no version yet, then the initial version
  * will be assigned.
  *
  * If the entity already has an assigned version, then the next
  * version will be assigned.
  */
  lazy private val versionApplier: T => T =
    (entity) =>
      entity.withVersion(
        entity.version
          .map(v => generator.nextVersion(v))
          .getOrElse(generator.initialVersion())
      )

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

  implicit val instantVersionToSqlTimestampMapper: JdbcType[InstantVersion] with BaseTypedType[InstantVersion] = MappedColumnType.base[InstantVersion, Timestamp](
    { instantVersion => new java.sql.Timestamp(instantVersion.instant.toEpochMilli) },
    { sqlTimestamp => InstantVersion(Instant.ofEpochMilli(sqlTimestamp.getTime)) })

  implicit val longInstantVersionToSqlTimestampMapper: JdbcType[LongInstantVersion] with BaseTypedType[LongInstantVersion] = MappedColumnType.base[LongInstantVersion, Long](
    { longInstantVersion => longInstantVersion.instant.toEpochMilli },
    { longTimestamp => LongInstantVersion(Instant.ofEpochMilli(longTimestamp)) })

  implicit val localDateTimeVersionToSqlTimestampMapper: JdbcType[LocalDateTimeVersion] with BaseTypedType[LocalDateTimeVersion] = MappedColumnType.base[LocalDateTimeVersion, Timestamp](
    {
      localDateTimeVersion =>
        new java.sql.Timestamp(
          localDateTimeVersion.localDateTime.atZone(DateTimeHelper.localDateTimeZone).toInstant.toEpochMilli
        )
    },
    {
      sqlTimestamp =>
        LocalDateTimeVersion(
          LocalDateTime.ofInstant(Instant.ofEpochMilli(sqlTimestamp.getTime), DateTimeHelper.localDateTimeZone)
        )
    }
  )

  implicit val longLocalDateTimeVersionToSqlTimestampMapper: JdbcType[LongLocalDateTimeVersion] with BaseTypedType[LongLocalDateTimeVersion] = MappedColumnType.base[LongLocalDateTimeVersion, Long](
    {
      localDateTimeVersion =>
        localDateTimeVersion.localDateTime.atZone(DateTimeHelper.localDateTimeZone).toInstant.toEpochMilli
    },
    {
      longTimestamp =>
        LongLocalDateTimeVersion(
          LocalDateTime.ofInstant(Instant.ofEpochMilli(longTimestamp), DateTimeHelper.localDateTimeZone)
        )
    }
  )
}
