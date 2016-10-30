package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.{LongVersionedEntity, Versioned}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

case class TestLongVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Long]) extends LongVersionedEntity[TestLongVersionedEntity, Int] {
  def withId(id: Int): TestLongVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Long): TestLongVersionedEntity = this.copy(version = Some(version))
}

class TestLongVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestLongVersionedEntity, Int, Long](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Long]]
  val tableQuery = TableQuery[TestLongVersionedEntities]
  type TableType = TestLongVersionedEntities

  class TestLongVersionedEntities(tag: slick.lifted.Tag) extends Table[TestLongVersionedEntity](tag, "TLV_ENTITY") with Versioned[Int, Long] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Long]("VERSION")

    def * = (id.?, price, version.?) <> ((TestLongVersionedEntity.apply _).tupled, TestLongVersionedEntity.unapply)
  }

}
