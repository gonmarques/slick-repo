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