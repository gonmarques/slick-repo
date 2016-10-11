package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.domain._
import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.repository.TestIntegerVersionedEntityRepository

class IntegerVersionedRepositoryTest extends AbstractRepositoryTest {

  val testIntegerVersionedEntityRepository = new TestIntegerVersionedEntityRepository

  "The Integer Versioned Repository" should "save an entity with an initial integer version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestIntegerVersionedEntity = executeAction(testIntegerVersionedEntityRepository.save(TestIntegerVersionedEntity(None, 2, None)))
    entity.version.get should equal(1)
    val readEntity = executeAction(testIntegerVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(1)
  }

  it should "update an entity incrementing the integer version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestIntegerVersionedEntity = executeAction(testIntegerVersionedEntityRepository.save(TestIntegerVersionedEntity(None, 2, None)))
    val readEntity = executeAction(testIntegerVersionedEntityRepository.findOne(entity.id.get))
    readEntity.version.get should equal(1)
    val updatedEntity = executeAction(testIntegerVersionedEntityRepository.update(readEntity.copy(price = 3)))
    updatedEntity.version.get should equal(2)
    val readUpdatedEntity = executeAction(testIntegerVersionedEntityRepository.findOne(entity.id.get))
    readUpdatedEntity.version.get should equal(2)
  }

  it should "updating an integer versioned entity that was meanwhile updated by other process throws exception" in {
    val exception =
    intercept[OptimisticLockException] {
      import scala.concurrent.ExecutionContext.Implicits.global
      val entity: TestIntegerVersionedEntity = executeAction(testIntegerVersionedEntityRepository.save(TestIntegerVersionedEntity(None, 2, None)))
      val readEntity = executeAction(testIntegerVersionedEntityRepository.findOne(entity.id.get))
      readEntity.version.get should equal(1)

      val updatedEntity = executeAction(testIntegerVersionedEntityRepository.update(readEntity.copy(price = 3)))
      updatedEntity.version.get should equal(2)

      executeAction(testIntegerVersionedEntityRepository.update(readEntity.copy(price = 4)))
    }
    exception.getMessage should equal("Failed to update entity of type com.byteslounge.slickrepo.domain.TestIntegerVersionedEntity. Expected version was not found: 1")
  }

}