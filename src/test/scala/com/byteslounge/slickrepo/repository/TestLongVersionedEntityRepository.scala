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

import com.byteslounge.slickrepo.meta.{ Versioned, VersionedEntity}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

case class TestLongVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Long]) extends VersionedEntity[TestLongVersionedEntity, Int, Long] {
  def withId(id: Int): TestLongVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Long): TestLongVersionedEntity = this.copy(version = Some(version))
}

class TestLongVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestLongVersionedEntity, Int, Long](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Long]]
  val tableQuery = TableQuery[TestLongVersionedEntities]
  type TableType = TestLongVersionedEntities

  class TestLongVersionedEntities(tag: slick.lifted.Tag) extends Table[TestLongVersionedEntity](tag, "TLV_ENTITY") with Versioned[Int, Long] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Long]("VERSION")

    def * = (id.?, price, version.?) <> ((TestLongVersionedEntity.apply _).tupled, TestLongVersionedEntity.unapply)
  }

}
