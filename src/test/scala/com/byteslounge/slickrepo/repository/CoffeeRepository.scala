package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

case class Coffee(override val id: Option[Int], brand: String) extends Entity[Coffee, Int]{
  def withId(id: Int): Coffee = this.copy(id = Some(id))
}

class CoffeeRepository(override val driver: JdbcProfile) extends Repository[Coffee, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Coffees]
  type TableType = Coffees

  class Coffees(tag: slick.lifted.Tag) extends Table[Coffee](tag, "COFFEE") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey)
    def brand = column[String]("BRAND")

    def * = (id.?, brand) <> ((Coffee.apply _).tupled, Coffee.unapply)
  }
}
