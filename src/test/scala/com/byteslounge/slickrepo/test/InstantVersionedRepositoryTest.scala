/*
 * MIT License
 *
 * Copyright (c) 2016 Gonçalo Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    val readEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get)).get
    readEntity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "update an entity (manual pk) incrementing the Instant version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.save(TestInstantVersionedEntity(Option(1), 2, None)))
    val readEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get)).get
    readEntity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
    val updatedEntity = executeAction(testInstantVersionedEntityRepository.update(readEntity.copy(price = 3)))
    updatedEntity.version.get should equal(Instant.parse("2016-01-04T01:01:05Z"))
    val readUpdatedEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get)).get
    readUpdatedEntity.version.get should equal(Instant.parse("2016-01-04T01:01:05Z"))
  }

  it should "updating an Instant versioned entity (manual pk) that was meanwhile updated by other process throws exception" in {
    val exception =
    intercept[OptimisticLockException] {
      import scala.concurrent.ExecutionContext.Implicits.global
      val entity: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.save(TestInstantVersionedEntity(Option(1), 2, None)))
      val readEntity = executeAction(testInstantVersionedEntityRepository.findOne(entity.id.get)).get
      readEntity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))

      val updatedEntity = executeAction(testInstantVersionedEntityRepository.update(readEntity.copy(price = 3)))
      updatedEntity.version.get should equal(Instant.parse("2016-01-04T01:01:05Z"))

      executeAction(testInstantVersionedEntityRepository.update(readEntity.copy(price = 4)))
    }
    exception.getMessage should equal("Failed to update entity of type com.byteslounge.slickrepo.repository.TestInstantVersionedEntity. Expected version was not found: 2016-01-03T01:01:02Z")
  }

  it should "perform a batch insert of instant versioned entities" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val batchInsertAction = testInstantVersionedEntityRepository.batchInsert(
      Seq(TestInstantVersionedEntity(Option(1), 2.2, None), TestInstantVersionedEntity(Option(2), 3.3, None), TestInstantVersionedEntity(Option(3), 4.4, None))
    )
    batchInsertAction.getClass.getName.contains("MultiInsertAction") should equal(true)
    val rowCount = executeAction(batchInsertAction)
    assertBatchInsertResult(rowCount)
    val entity1: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.findOne(1)).get
    val entity2: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.findOne(2)).get
    val entity3: TestInstantVersionedEntity = executeAction(testInstantVersionedEntityRepository.findOne(3)).get
    entity1.price should equal(2.2)
    entity1.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
    entity2.price should equal(3.3)
    entity2.version.get should equal(Instant.parse("2016-01-04T01:01:05Z"))
    entity3.price should equal(4.4)
    entity3.version.get should equal(Instant.parse("2016-01-05T01:01:07Z"))
  }
}
