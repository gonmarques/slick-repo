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
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class VersionHelperTest extends FlatSpec with Matchers with BeforeAndAfter {

  before {
    MockDateTimeHelper.start()
    MockDateTimeHelper.mock(
      Instant.parse("2016-01-03T01:01:02Z")
    )
  }

  "The Integer Version Generator" should "generate the integer initial value" in {
    VersionGenerator.intVersionGenerator.initialVersion() should equal(1)
  }

  it should "generate the next integer value" in {
    VersionGenerator.intVersionGenerator.nextVersion(1) should equal(2)
  }

  "The Long Version Generator" should "generate the long initial value" in {
    VersionGenerator.longVersionGenerator.initialVersion() should equal(1L)
  }

  it should "generate the next long value" in {
    VersionGenerator.longVersionGenerator.nextVersion(1L) should equal(2L)
  }

  "The Instant Version Generator" should "generate the instant initial value" in {
    VersionGenerator.instantVersionGenerator.initialVersion() should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

  it should "generate the next instant value" in {
    VersionGenerator.instantVersionGenerator.nextVersion(Instant.parse("2016-01-03T01:01:02Z")) should equal(Instant.parse("2016-01-03T01:01:02Z"))
  }

}