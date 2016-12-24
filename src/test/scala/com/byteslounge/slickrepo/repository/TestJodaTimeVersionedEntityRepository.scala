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

import com.byteslounge.slickrepo.datetime.DateTimeHelper
import com.byteslounge.slickrepo.meta.{Version, Versioned, VersionedEntity}
import org.joda.time.Instant
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile
import jodaTimeVersionedImplicits._

case class TestJodaTimeVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Instant]) extends VersionedEntity[TestJodaTimeVersionedEntity, Int, Instant] {
  def withId(id: Int): TestJodaTimeVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Instant): TestJodaTimeVersionedEntity = this.copy(version = Some(version))
}

class TestJodaTimeVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestJodaTimeVersionedEntity, Int, Instant](driver) {

  import driver.api._

  implicit val jodaTimeInstantToSqlTimestampMapper = MappedColumnType.base[Instant, Timestamp](
    { instant => new java.sql.Timestamp(instant.getMillis) },
    { sqlTimestamp => new Instant(sqlTimestamp.getTime) })

  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Instant]]
  val tableQuery = TableQuery[TestJodaTimeVersionedEntities]
  type TableType = TestJodaTimeVersionedEntities

  class TestJodaTimeVersionedEntities(tag: slick.lifted.Tag) extends Table[TestJodaTimeVersionedEntity](tag, "TJTV_ENTITY") with Versioned[Int, Instant] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Instant]("VERSION")

    def * = (id.?, price, version.?) <> ((TestJodaTimeVersionedEntity.apply _).tupled, TestJodaTimeVersionedEntity.unapply)
  }

}

object jodaTimeVersionedImplicits {
  implicit def initialVersion(): Version[Instant] = {
    Version(currentInstant())
  }

  implicit  def nextVersion(currentVersion: Instant): Version[Instant] = {
    Version(currentInstant())
  }

  private def currentInstant(): Instant = {
    new Instant(DateTimeHelper.currentInstant.toEpochMilli)
  }
}