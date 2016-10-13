package com.byteslounge.slickrepo.mysql.domain

import com.byteslounge.slickrepo.meta.Entity
import com.byteslounge.slickrepo.meta.Keyed
import slick.driver.MySQLDriver.api._

case class Person(override val id: Option[Int] = None, name: String) extends Entity[Person, Int]{
  def withId(id: Int): Person = this.copy(id = Some(id))
}

class Persons(tag: slick.lifted.Tag) extends Table[Person](tag, "PERSON") with Keyed[Int] {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  
  def * = (id.?, name) <> ((Person.apply _).tupled, Person.unapply _)
}
