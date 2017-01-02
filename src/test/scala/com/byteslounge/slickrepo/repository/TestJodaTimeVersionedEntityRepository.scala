package com.byteslounge.slickrepo.repository

import java.sql.Timestamp

import com.byteslounge.slickrepo.datetime.DateTimeHelper
import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import org.joda.time.Instant
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

case class TestJodaTimeVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[Instant]) extends VersionedEntity[TestJodaTimeVersionedEntity, Int, Instant] {
  def withId(id: Int): TestJodaTimeVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: Instant): TestJodaTimeVersionedEntity = this.copy(version = Some(version))
}

class TestJodaTimeVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[TestJodaTimeVersionedEntity, Int, Instant](driver) {

  import driver.api._

  implicit val jodaTimeInstantToSqlTimestampMapper = MappedColumnType.base[Instant, Timestamp](
    { instant => new java.sql.Timestamp(instant.getMillis) },
    { sqlTimestamp => new Instant(sqlTimestamp.getTime) })

  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Instant]]
  val tableQuery = TableQuery[TestJodaTimeVersionedEntities]
  type TableType = TestJodaTimeVersionedEntities

  class TestJodaTimeVersionedEntities(tag: slick.lifted.Tag) extends Table[TestJodaTimeVersionedEntity](tag, "TJTV_ENTITY") with Versioned[Int, Instant] {
    def id = column[Int]("ID", O.PrimaryKey)
    def price = column[Double]("PRICE")
    def version = column[Instant]("VERSION")

    def * = (id.?, price, version.?) <> ((TestJodaTimeVersionedEntity.apply _).tupled, TestJodaTimeVersionedEntity.unapply)
  }

}

object jodaTimeVersionedImplicits {
  implicit def initialVersion(): Instant = {
    currentInstant()
  }

  implicit  def nextVersion(currentVersion: Instant): Instant = {
    currentInstant()
  }

  private def currentInstant(): Instant = {
    new Instant(DateTimeHelper.currentInstant.toEpochMilli)
  }
}