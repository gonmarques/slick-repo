/*
 * MIT License
 *
 * Copyright (c) 2019 Gonçalo Marques
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

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import slick.lifted.ProvenShape


case class CompositeId(idOne: Int, idTwo: Int)

case class Composite(override val id: Option[CompositeId], someField: String) extends Entity[Composite, CompositeId] {
  def withId(id: CompositeId): Composite = this.copy(id = Some(id))
}

class CompositeRepository(override val driver: JdbcProfile) extends Repository[Composite, CompositeId](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[CompositeId]]
  val tableQuery = TableQuery[Composites]
  type TableType = Composites

  class Composites(tag: slick.lifted.Tag) extends Table[Composite](tag, "COMPOSITE") with Keyed[CompositeId] {

    implicit def tupleToId(tuple: (Option[Int], Option[Int])): Option[CompositeId] = tuple match {
      case (Some(idOne), Some(idTwo)) => Some(CompositeId(idOne, idTwo))
      case _                              => None
    }

    def id = (idOne, idTwo) <> (CompositeId.tupled, CompositeId.unapply)
    def idOne = column[Int]("ID_ONE")
    def idTwo = column[Int]("ID_TWO")
    def someField = column[String]("SOME_FIELD")

    def pk = primaryKey("COMPOSITE_PK", (idOne, idTwo))

    def * : ProvenShape[Composite] = {
      val shapedValue = (idOne, idTwo, someField).shaped

      shapedValue.<>({
        tuple =>
          new Composite(Option(new CompositeId(tuple._1, tuple._2)), tuple._3)
      }, {
        (u: Composite) =>
          Some {
            (
              u.id.map(_.idOne).get,
              u.id.map(_.idTwo).get,
            u.someField)
          }
      })
    }
  }
}
