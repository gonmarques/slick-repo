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

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.scalaversion.JdbcProfile

case class TestIntegerVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Int]) extends VersionedEntity[TestIntegerVersionedEntity, Int, Int] {
  def withId(id: Int): TestIntegerVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Int): TestIntegerVersionedEntity = this.copy(version = Some(version))
}

class TestIntegerVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestIntegerVersionedEntity, Int, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[TestIntegerVersionedEntities]
  type TableType = TestIntegerVersionedEntities

  class TestIntegerVersionedEntities(tag: slick.lifted.Tag) extends Table[TestIntegerVersionedEntity](tag, "TIV_ENTITY") with Versioned[Int, Int] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Int]("VERSION")

    def * = (id.?, price, version.?) <> ((TestIntegerVersionedEntity.apply _).tupled, TestIntegerVersionedEntity.unapply)
  }

}
