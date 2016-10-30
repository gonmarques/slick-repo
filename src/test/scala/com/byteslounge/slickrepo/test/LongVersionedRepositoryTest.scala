package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.repository.TestLongVersionedEntity

abstract class LongVersionedRepositoryTest(override val config: Config) extends AbstractRepositoryTest(config) {

  "The Long Versioned Repository" should "save an entity (manual pk) with an initial long version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestLongVersionedEntity = executeAction(testLongVersionedEntityRepository.save(TestLongVersionedEntity(Option(1), 2, None)))
    entity.version.get should equal(1)
    val readEntity = executeAction(testLongVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(1)
  }

  it should "update an entity (manual pk) incrementing the long version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestLongVersionedEntity = executeAction(testLongVersionedEntityRepository.save(TestLongVersionedEntity(Option(1), 2, None)))
    val readEntity = executeAction(testLongVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(1)
    val updatedEntity = executeAction(testLongVersionedEntityRepository.update(readEntity.copy(price = 3)))
    updatedEntity.version.get should equal(2)
    val readUpdatedEntity = executeAction(testLongVersionedEntityRepository.findOne(entity.id.get))
    readUpdatedEntity.version.get should equal(2)
  }

  it should "updating an long versioned entity (manual pk) that was meanwhile updated by other process throws exception" in {
    val exception =
    intercept[OptimisticLockException] {
      import scala.concurrent.ExecutionContext.Implicits.global
      val entity: TestLongVersionedEntity = executeAction(testLongVersionedEntityRepository.save(TestLongVersionedEntity(Option(1), 2, None)))
      val readEntity = executeAction(testLongVersionedEntityRepository.findOne(entity.id.get))
      readEntity.version.get should equal(1)

      val updatedEntity = executeAction(testLongVersionedEntityRepository.update(readEntity.copy(price = 3)))
      updatedEntity.version.get should equal(2)

      executeAction(testLongVersionedEntityRepository.update(readEntity.copy(price = 4)))
    }
    exception.getMessage should equal("Failed to update entity of type com.byteslounge.slickrepo.repository.TestLongVersionedEntity. Expected version was not found: 1")
  }

}