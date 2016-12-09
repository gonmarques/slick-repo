package com.byteslounge.slickrepo.version

import java.time.Instant

import com.byteslounge.slickrepo.datetime.MockDateTimeHelper
import com.byteslounge.slickrepo.meta.Entity
import com.byteslounge.slickrepo.repository.{TestInstantVersionedEntity, TestIntegerVersionedEntity, TestLongVersionedEntity}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class VersionHelperTest extends FlatSpec with Matchers with BeforeAndAfter {

  before {
    MockDateTimeHelper.start()
    MockDateTimeHelper.mock(
      Instant.parse("2016-01-03T01:01:02Z")
    )
  }

  "The Version Helper" should "update an entity version with the integer initial value if the version is not set" in {
    val entity = new VersionHelper[TestIntegerVersionedEntity].process(TestIntegerVersionedEntity(None, 2, None))
    entity.version.get should equal(1)
  }

  it should "update an entity version with the next integer value if the version is set" in {
    val entity = new VersionHelper[TestIntegerVersionedEntity].process(TestIntegerVersionedEntity(None, 2, Some(1)))
    entity.version.get should equal(2)
  }

  it should "update an entity version with the long initial value if the version is not set" in {
    val entity = new VersionHelper[TestLongVersionedEntity].process(TestLongVersionedEntity(None, 2, None))
    entity.version.get should equal(1)
  }

  it should "update an entity version with the next long value if the version is set" in {
    val entity = new VersionHelper[TestLongVersionedEntity].process(TestLongVersionedEntity(None, 2, Some(1)))
    entity.version.get should equal(2)
  }

  it should "update an entity version with the instant initial value if the version is not set" in {
    val entity = new VersionHelper[TestInstantVersionedEntity].process(TestInstantVersionedEntity(None, 2, None))
    entity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "update an entity version with the next instant value if the version is set" in {
    val entity = new VersionHelper[TestInstantVersionedEntity].process(TestInstantVersionedEntity(None, 2, Some(Instant.parse("2016-01-01T01:01:01Z"))))
    entity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "throw an exception while processing an entity that is not versioned" in {
    val exception =
      intercept[IllegalStateException] {
        new VersionHelper[NonVersionedEntity].process(NonVersionedEntity())
      }
    exception.getMessage should equal("Versioned Entity version type is not supported: com.byteslounge.slickrepo.version.VersionHelperTest$NonVersionedEntity")
  }

  case class NonVersionedEntity() extends Entity[NonVersionedEntity, Int] {
    override def withId(id: Int): NonVersionedEntity = this
  }
}
