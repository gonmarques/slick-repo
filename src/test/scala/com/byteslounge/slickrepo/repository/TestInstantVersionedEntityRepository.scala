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

import java.time.Instant

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.scalaversion.JdbcProfile

case class TestInstantVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Instant]) extends VersionedEntity[TestInstantVersionedEntity, Int, Instant] {
  def withId(id: Int): TestInstantVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Instant): TestInstantVersionedEntity = this.copy(version = Some(version))
}

class TestInstantVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestInstantVersionedEntity, Int, Instant](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Instant]]
  val tableQuery = TableQuery[TestInstantVersionedEntities]
  type TableType = TestInstantVersionedEntities

  class TestInstantVersionedEntities(tag: slick.lifted.Tag) extends Table[TestInstantVersionedEntity](tag, "TINSV_ENTITY") with Versioned[Int, Instant] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Instant]("VERSION")

    def * = (id.?, price, version.?) <> ((TestInstantVersionedEntity.apply _).tupled, TestInstantVersionedEntity.unapply)
  }

}
