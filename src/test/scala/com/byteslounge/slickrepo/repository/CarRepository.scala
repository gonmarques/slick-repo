package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

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
