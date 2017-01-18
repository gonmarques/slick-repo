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

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import slick.ast.BaseTypedType

case class Car(override val id: Option[Int] = None, brand: String, idPerson: Int) extends Entity[Car, Int] {
  def withId(id: Int): Car = this.copy(id = Some(id))
}

class CarRepository(override val driver: JdbcProfile) extends Repository[Car, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Cars]
  type TableType = Cars

  lazy val personRepository = new PersonRepository(driver)

  class Cars(tag: slick.lifted.Tag) extends Table[Car](tag, "CAR") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def brand = column[String]("BRAND")
    def idPerson = column[Int]("ID_PERSON")

    def * = (id.?, brand, idPerson) <> ((Car.apply _).tupled, Car.unapply)

    def person = foreignKey("PERSON_FK", idPerson, personRepository.tableQuery)(_.id)
  }
}
