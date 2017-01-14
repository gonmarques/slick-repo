/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
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

}