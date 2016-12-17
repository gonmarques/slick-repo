package com.byteslounge.slickrepo.test

import java.time.Instant

import com.byteslounge.slickrepo.datetime.MockDateTimeHelper
import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.repository.TestJodaTimeVersionedEntity
import com.byteslounge.slickrepo.version.{JodaTimeVersionGenerator, VersionedEntityTypes}

abstract class JodaTimeVersionedRepositoryTest(override val config: Config) extends AbstractRepositoryTest(config) {

  {
    VersionedEntityTypes.add(new JodaTimeVersionGenerator)
  }

  override def prepareTest() {
    MockDateTimeHelper.start()
    MockDateTimeHelper.mock(
      Instant.parse("2016-01-03T01:01:02Z"),
      Instant.parse("2016-01-04T01:01:05Z"),
      Instant.parse("2016-01-05T01:01:07Z")
    )
  }

  "The Joda Time Versioned Repository" should "save an entity (manual pk) with an initial JodaTime Instant version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestJodaTimeVersionedEntity = executeAction(testJodaTimeVersionedEntityRepository.save(TestJodaTimeVersionedEntity(Option(1), 2, None)))
    entity.version.get should equal(org.joda.time.Instant.parse("2016-01-03T01:01:02Z"))
    val readEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "update an entity (manual pk) incrementing the Joda Time Instant version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestJodaTimeVersionedEntity = executeAction(testJodaTimeVersionedEntityRepository.save(TestJodaTimeVersionedEntity(Option(1), 2, None)))
    val readEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-03T01:01:02Z"))
    val updatedEntity = executeAction(testJodaTimeVersionedEntityRepository.update(readEntity.copy(price = 3)))
    updatedEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-04T01:01:05Z"))
    val readUpdatedEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get))
    readUpdatedEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-04T01:01:05Z"))
  }

  it should "updating a Joda Time Instant versioned entity (manual pk) that was meanwhile updated by other process throws exception" in {
    val exception =
    intercept[OptimisticLockException] {
      import scala.concurrent.ExecutionContext.Implicits.global
      val entity: TestJodaTimeVersionedEntity = executeAction(testJodaTimeVersionedEntityRepository.save(TestJodaTimeVersionedEntity(Option(1), 2, None)))
      val readEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get))
      readEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-03T01:01:02Z"))

      val updatedEntity = executeAction(testJodaTimeVersionedEntityRepository.update(readEntity.copy(price = 3)))
      updatedEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-04T01:01:05Z"))

      executeAction(testJodaTimeVersionedEntityRepository.update(readEntity.copy(price = 4)))
    }
    exception.getMessage should equal("Failed to update entity of type com.byteslounge.slickrepo.repository.TestJodaTimeVersionedEntity. Expected version was not found: 2016-01-03T01:01:02.000Z")
  }

}