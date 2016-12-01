package com.byteslounge.slickrepo.test

import java.time.Instant

import com.byteslounge.slickrepo.datetime.MockDateTimeHelper
import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.repository.TestInstantVersionedEntity

abstract class InstantVersionedRepositoryTest(override val config: Config) extends AbstractRepositoryTest(config) {

  override def prepareTest() {
    MockDateTimeHelper.start()
    MockDateTimeHelper.mock(
      Instant.parse("2016-01-03T01:01:02Z"),
      Instant.parse("2016-01-04T01:01:05Z"),
      Instant.parse("2016-01-05T01:01:07Z")
    )
  }

  "The Instant Versioned Repository" should "save an entity (manual pk) with an initial Instant version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.save(TestInstantVersionedEntity(Option(1), 2, None)))
    entity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
    val readEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "update an entity (manual pk) incrementing the Instant version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.save(TestInstantVersionedEntity(Option(1), 2, None)))
    val readEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
    val updatedEntity = executeAction(testInstantVersionedEntityRepository.update(readEntity.copy(price = 3)))
    updatedEntity.version.get should equal(Instant.parse("2016-01-04T01:01:05Z"))
    val readUpdatedEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get))
    readUpdatedEntity.version.get should equal(Instant.parse("2016-01-04T01:01:05Z"))
  }

  it should "updating an Instant versioned entity (manual pk) that was meanwhile updated by other process throws exception" in {
    val exception =
    intercept[OptimisticLockException] {
      import scala.concurrent.ExecutionContext.Implicits.global
      val entity: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.save(TestInstantVersionedEntity(Option(1), 2, None)))
      val readEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get))
      readEntity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))

      val updatedEntity = executeAction(testInstantVersionedEntityRepository.update(readEntity.copy(price = 3)))
      updatedEntity.version.get should equal(Instant.parse("2016-01-04T01:01:05Z"))

      executeAction(testInstantVersionedEntityRepository.update(readEntity.copy(price = 4)))
    }
    exception.getMessage should equal("Failed to update entity of type com.byteslounge.slickrepo.repository.TestInstantVersionedEntity. Expected version was not found: 2016-01-03T01:01:02Z")
  }

}