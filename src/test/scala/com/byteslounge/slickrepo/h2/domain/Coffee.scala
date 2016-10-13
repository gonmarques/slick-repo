package com.byteslounge.slickrepo.h2.domain

import com.byteslounge.slickrepo.meta.Entity
import com.byteslounge.slickrepo.meta.Keyed
import slick.driver.H2Driver.api._

case class Coffee(override val id: Option[Int], brand: String) extends Entity[Coffee, Int]{
  def withId(id: Int): Coffee = this.copy(id = Some(id))
}

class Coffees(tag: slick.lifted.Tag) extends Table[Coffee](tag, "COFFEE") with Keyed[Int] {
  def id = column[Int]("ID", O.PrimaryKey)
  def brand = column[String]("BRAND")
  
  def * = (id.?, brand) <> ((Coffee.apply _).tupled, Coffee.unapply _)
}
