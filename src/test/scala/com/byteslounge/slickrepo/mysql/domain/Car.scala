package com.byteslounge.slickrepo.mysql.domain

import com.byteslounge.slickrepo.meta.Entity
import com.byteslounge.slickrepo.meta.Keyed
import slick.driver.MySQLDriver.api._

case class Car(override val id: Option[Int] = None, brand: String, idPerson: Int) extends Entity[Car, Int] {
  def withId(id: Int): Car = this.copy(id = Some(id))
}

class Cars(tag: slick.lifted.Tag) extends Table[Car](tag, "CAR") with Keyed[Int] {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def brand = column[String]("BRAND")
  def idPerson = column[Int]("ID_PERSON")
  
  def * = (id.?, brand, idPerson) <> ((Car.apply _).tupled, Car.unapply _)
  
  def person = foreignKey("PERSON_FK", idPerson, TableQuery[Persons])(_.id)
}
