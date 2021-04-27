/*
 * MIT License
 *
 * Copyright (c) 2017 Gonçalo Marques
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
import com.byteslounge.slickrepo.version.LongLocalDateTimeVersion
import slick.ast.BaseTypedType

case class TestLongLocalDateTimeVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[LongLocalDateTimeVersion]) extends VersionedEntity[TestLongLocalDateTimeVersionedEntity, Int, LongLocalDateTimeVersion] {
  def withId(id: Int): TestLongLocalDateTimeVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: LongLocalDateTimeVersion): TestLongLocalDateTimeVersionedEntity = this.copy(version = Some(version))
}

class TestLongLocalDateTimeVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestLongLocalDateTimeVersionedEntity, Int, LongLocalDateTimeVersion] {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[LongLocalDateTimeVersion]]
  val tableQuery = TableQuery[TestLongLocalDateTimeVersionedEntities]
  type TableType = TestLongLocalDateTimeVersionedEntities

  class TestLongLocalDateTimeVersionedEntities(tag: slick.lifted.Tag) extends Table[TestLongLocalDateTimeVersionedEntity](tag, "TLONGLDTV_ENTITY") with Versioned[Int, LongLocalDateTimeVersion] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[LongLocalDateTimeVersion]("VERSION")

    def * = (id.?, price, version.?) <> ((TestLongLocalDateTimeVersionedEntity.apply _).tupled, TestLongLocalDateTimeVersionedEntity.unapply)
  }

}
