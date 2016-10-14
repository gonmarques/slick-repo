package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

case class Person(override val id: Option[Int] = None, name: String) extends Entity[Person, Int]{
  def withId(id: Int): Person = this.copy(id = Some(id))
}

class PersonRepository(override val driver: JdbcProfile) extends Repository[Person, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Persons]
  type TableType = Persons

  lazy val carRepository = new CarRepository(driver)

  class Persons(tag: slick.lifted.Tag) extends Table[Person](tag, "PERSON") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")

    def * = (id.?, name) <> ((Person.apply _).tupled, Person.unapply)
  }

  def findWithCarsOrderByIdAscAndCarIdDesc(): DBIO[Seq[(Person, Car)]] = {
    (tableQuery
      join carRepository.tableQuery on (_.id === _.idPerson))
      .map(x => (x._1, x._2))
      .sortBy(_._2.id.desc)
      .sortBy(_._1.id.asc)
      .result
  }

}
