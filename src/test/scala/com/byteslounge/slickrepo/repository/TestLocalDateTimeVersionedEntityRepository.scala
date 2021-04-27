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
import com.byteslounge.slickrepo.version.LocalDateTimeVersion
import slick.ast.BaseTypedType

case class TestLocalDateTimeVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[LocalDateTimeVersion]) extends VersionedEntity[TestLocalDateTimeVersionedEntity, Int, LocalDateTimeVersion] {
  def withId(id: Int): TestLocalDateTimeVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: LocalDateTimeVersion): TestLocalDateTimeVersionedEntity = this.copy(version = Some(version))
}

class TestLocalDateTimeVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestLocalDateTimeVersionedEntity, Int, LocalDateTimeVersion] {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[LocalDateTimeVersion]]
  val tableQuery = TableQuery[TestLocalDateTimeVersionedEntities]
  type TableType = TestLocalDateTimeVersionedEntities

  class TestLocalDateTimeVersionedEntities(tag: slick.lifted.Tag) extends Table[TestLocalDateTimeVersionedEntity](tag, "TLDTV_ENTITY") with Versioned[Int, LocalDateTimeVersion] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[LocalDateTimeVersion]("VERSION")

    def * = (id.?, price, version.?) <> ((TestLocalDateTimeVersionedEntity.apply _).tupled, TestLocalDateTimeVersionedEntity.unapply)
  }

}
