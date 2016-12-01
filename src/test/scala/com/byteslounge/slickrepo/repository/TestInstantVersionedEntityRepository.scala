package com.byteslounge.slickrepo.repository

import java.time.Instant

import com.byteslounge.slickrepo.meta.{InstantVersionedEntity, Versioned}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

case class TestInstantVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Instant]) extends InstantVersionedEntity[TestInstantVersionedEntity, Int] {
  def withId(id: Int): TestInstantVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Instant): TestInstantVersionedEntity = this.copy(version = Some(version))
}

class TestInstantVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestInstantVersionedEntity, Int, Instant](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Instant]]
  val tableQuery = TableQuery[TestInstantVersionedEntities]
  type TableType = TestInstantVersionedEntities

  class TestInstantVersionedEntities(tag: slick.lifted.Tag) extends Table[TestInstantVersionedEntity](tag, "TINSV_ENTITY") with Versioned[Int, Instant] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Instant]("VERSION")

    def * = (id.?, price, version.?) <> ((TestInstantVersionedEntity.apply _).tupled, TestInstantVersionedEntity.unapply)
  }

}
