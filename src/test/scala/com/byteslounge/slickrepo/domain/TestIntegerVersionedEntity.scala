package com.byteslounge.slickrepo.domain

import com.byteslounge.slickrepo.meta.{IntVersionedEntity, Versioned}
import slick.driver.H2Driver.api._

case class TestIntegerVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Int]) extends IntVersionedEntity[TestIntegerVersionedEntity, Int] {
  def withId(id: Int): TestIntegerVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Int): TestIntegerVersionedEntity = this.copy(version = Some(version))
}

class TestIntegerVersionedEntities(tag: slick.lifted.Tag) extends Table[TestIntegerVersionedEntity](tag, "TIV_Entity") with Versioned[Int, Int] {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def price = column[Double]("PRICE")
  def version = column[Int]("VERSION")

  def * = (id.?, price, version.?) <> ((TestIntegerVersionedEntity.apply _).tupled, TestIntegerVersionedEntity.unapply _)
}
