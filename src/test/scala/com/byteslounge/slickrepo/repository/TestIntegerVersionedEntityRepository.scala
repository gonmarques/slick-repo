package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.{IntVersionedEntity, Versioned}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

case class TestIntegerVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Int]) extends IntVersionedEntity[TestIntegerVersionedEntity, Int] {
  def withId(id: Int): TestIntegerVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Int): TestIntegerVersionedEntity = this.copy(version = Some(version))
}

class TestIntegerVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestIntegerVersionedEntity, Int, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[TestIntegerVersionedEntities]
  type TableType = TestIntegerVersionedEntities

  class TestIntegerVersionedEntities(tag: slick.lifted.Tag) extends Table[TestIntegerVersionedEntity](tag, "TIV_ENTITY") with Versioned[Int, Int] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Int]("VERSION")

    def * = (id.?, price, version.?) <> ((TestIntegerVersionedEntity.apply _).tupled, TestIntegerVersionedEntity.unapply)
  }

}
