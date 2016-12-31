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

package com.byteslounge.slickrepo.version

import java.time.Instant

import com.byteslounge.slickrepo.datetime.MockDateTimeHelper
import com.byteslounge.slickrepo.exception.VersionGeneratorNotFoundException
import com.byteslounge.slickrepo.meta.VersionedEntity
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
    val entity = new VersionHelper[TestIntegerVersionedEntity, Int].process(TestIntegerVersionedEntity(None, 2, None))
    entity.version.get should equal(1)
  }

  it should "update an entity version with the next integer value if the version is set" in {
    val entity = new VersionHelper[TestIntegerVersionedEntity, Int].process(TestIntegerVersionedEntity(None, 2, Some(1)))
    entity.version.get should equal(2)
  }

  it should "update an entity version with the long initial value if the version is not set" in {
    val entity = new VersionHelper[TestLongVersionedEntity, Long].process(TestLongVersionedEntity(None, 2, None))
    entity.version.get should equal(1)
  }

  it should "update an entity version with the next long value if the version is set" in {
    val entity = new VersionHelper[TestLongVersionedEntity, Long].process(TestLongVersionedEntity(None, 2, Some(1)))
    entity.version.get should equal(2)
  }

  it should "update an entity version with the instant initial value if the version is not set" in {
    val entity = new VersionHelper[TestInstantVersionedEntity, Instant].process(TestInstantVersionedEntity(None, 2, None))
    entity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "update an entity version with the next instant value if the version is set" in {
    val entity = new VersionHelper[TestInstantVersionedEntity, Instant].process(TestInstantVersionedEntity(None, 2, Some(Instant.parse("2016-01-01T01:01:01Z"))))
    entity.version.get should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "throw an error while generating the initial version for an entity version type which generator was not registered" in {
    val exception =
      intercept[VersionGeneratorNotFoundException] {
        new VersionHelper[TestStringVersionedEntity, String].process(TestStringVersionedEntity(None, 2, None))
      }
    exception.getMessage should equal("Could not find a VersionGenerator for version field of type: String. A VersionGenerator for type String should be implemented and registered via VersionHelper#add()")
  }

  it should "throw an error while generating the next version for an entity version type which generator was not registered" in {
    val exception =
      intercept[VersionGeneratorNotFoundException] {
        new VersionHelper[TestStringVersionedEntity, String].process(TestStringVersionedEntity(None, 2, Some("initialVersion")))
      }
    exception.getMessage should equal("Could not find a VersionGenerator for version field of type: String. A VersionGenerator for type String should be implemented and registered via VersionHelper#add()")
  }
}

case class TestStringVersionedEntity(override val id: Option[Int], price: Double, override val version: Option[String]) extends VersionedEntity[TestStringVersionedEntity, Int, String] {
  def withId(id: Int): TestStringVersionedEntity = this.copy(id = Some(id))
  def withVersion(version: String): TestStringVersionedEntity = this.copy(version = Some(version))
}
