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

import java.time.Instant

import com.byteslounge.slickrepo.datetime.MockDateTimeHelper
import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.repository.TestJodaTimeVersionedEntity

abstract class JodaTimeVersionedRepositoryTest(override val config: Config) extends AbstractRepositoryTest(config) {

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
    val readEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get)).get
    readEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "update an entity (manual pk) incrementing the Joda Time Instant version field value" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: TestJodaTimeVersionedEntity = executeAction(testJodaTimeVersionedEntityRepository.save(TestJodaTimeVersionedEntity(Option(1), 2, None)))
    val readEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get)).get
    readEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-03T01:01:02Z"))
    val updatedEntity = executeAction(testJodaTimeVersionedEntityRepository.update(readEntity.copy(price = 3)))
    updatedEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-04T01:01:05Z"))
    val readUpdatedEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get)).get
    readUpdatedEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-04T01:01:05Z"))
  }

  it should "updating a Joda Time Instant versioned entity (manual pk) that was meanwhile updated by other process throws exception" in {
    val exception =
    intercept[OptimisticLockException] {
      import scala.concurrent.ExecutionContext.Implicits.global
      val entity: TestJodaTimeVersionedEntity = executeAction(testJodaTimeVersionedEntityRepository.save(TestJodaTimeVersionedEntity(Option(1), 2, None)))
      val readEntity = executeAction(testJodaTimeVersionedEntityRepository.findOne(entity.id.get)).get
      readEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-03T01:01:02Z"))

      val updatedEntity = executeAction(testJodaTimeVersionedEntityRepository.update(readEntity.copy(price = 3)))
      updatedEntity.version.get should equal(org.joda.time.Instant.parse("2016-01-04T01:01:05Z"))

      executeAction(testJodaTimeVersionedEntityRepository.update(readEntity.copy(price = 4)))
    }
    exception.getMessage should equal("Failed to update entity of type com.byteslounge.slickrepo.repository.TestJodaTimeVersionedEntity. Expected version was not found: 2016-01-03T01:01:02.000Z")
  }

}