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

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import com.byteslounge.slickrepo.version.LongInstantVersion
import slick.ast.BaseTypedType

case class TestLongInstantVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[LongInstantVersion]) extends VersionedEntity[TestLongInstantVersionedEntity, Int, LongInstantVersion] {
  def withId(id: Int): TestLongInstantVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: LongInstantVersion): TestLongInstantVersionedEntity = this.copy(version = Some(version))
}

class TestLongInstantVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestLongInstantVersionedEntity, Int, LongInstantVersion] {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[LongInstantVersion]]
  val tableQuery = TableQuery[TestLongInstantVersionedEntities]
  type TableType = TestLongInstantVersionedEntities

  class TestLongInstantVersionedEntities(tag: slick.lifted.Tag) extends Table[TestLongInstantVersionedEntity](tag, "TLONGINSV_ENTITY") with Versioned[Int, LongInstantVersion] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[LongInstantVersion]("VERSION")

    def * = (id.?, price, version.?) <> ((TestLongInstantVersionedEntity.apply _).tupled, TestLongInstantVersionedEntity.unapply)
  }

}
