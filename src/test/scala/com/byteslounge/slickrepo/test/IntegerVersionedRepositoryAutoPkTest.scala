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

import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.repository.TestIntegerVersionedAutoPkEntity

abstract class IntegerVersionedRepositoryAutoPkTest(override val config: Config) extends IntegerVersionedRepositoryTest(config) {

  "The Integer Versioned Repository (Auto PK entity)" should "save an entity (auto pk) with an initial integer version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestIntegerVersionedAutoPkEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.save(TestIntegerVersionedAutoPkEntity(None, 2, None)))
    entity.version.get should equal(1)
    val readEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.findOne(entity.id.get)).get
    readEntity.version.get should equal(1)
  }

  it should "update an entity (auto pk) incrementing the integer version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestIntegerVersionedAutoPkEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.save(TestIntegerVersionedAutoPkEntity(None, 2, None)))
    val readEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.findOne(entity.id.get)).get
    readEntity.version.get should equal(1)
    val updatedEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.update(readEntity.copy(price = 3)))
    updatedEntity.version.get should equal(2)
    val readUpdatedEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.findOne(entity.id.get)).get
    readUpdatedEntity.version.get should equal(2)
  }

  it should "updating an integer versioned entity (auto pk) that was meanwhile updated by other process throws exception" in {
    val exception =
      intercept[OptimisticLockException] {
        import scala.concurrent.ExecutionContext.Implicits.global
        val entity: TestIntegerVersionedAutoPkEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.save(TestIntegerVersionedAutoPkEntity(None, 2, None)))
        val readEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.findOne(entity.id.get)).get
        readEntity.version.get should equal(1)

        val updatedEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.update(readEntity.copy(price = 3)))
        updatedEntity.version.get should equal(2)

        executeAction(testIntegerVersionedAutoPkEntityRepository.update(readEntity.copy(price = 4)))
      }
    exception.getMessage should equal("Failed to update entity of type com.byteslounge.slickrepo.repository.TestIntegerVersionedAutoPkEntity. Expected version was not found: 1")
  }

  it should "perform a batch insert of auto pk integer versioned entities" in {
    val batchInsertAction = testIntegerVersionedAutoPkEntityRepository.batchInsert(
      Seq(TestIntegerVersionedAutoPkEntity(None, 2.2, None), TestIntegerVersionedAutoPkEntity(None, 3.3, None), TestIntegerVersionedAutoPkEntity(None, 4.4, None))
    )
    batchInsertAction.getClass.getName.contains("MultiInsertAction") should equal(true)
    val rowCount = executeAction(batchInsertAction)
    assertBatchInsertResult(rowCount)
    val entity1: TestIntegerVersionedAutoPkEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.findOne(1)).get
    val entity2: TestIntegerVersionedAutoPkEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.findOne(2)).get
    val entity3: TestIntegerVersionedAutoPkEntity = executeAction(testIntegerVersionedAutoPkEntityRepository.findOne(3)).get
    entity1.price should equal(2.2)
    entity1.version.get should equal(1)
    entity2.price should equal(3.3)
    entity2.version.get should equal(1)
    entity3.price should equal(4.4)
    entity3.version.get should equal(1)
  }
}