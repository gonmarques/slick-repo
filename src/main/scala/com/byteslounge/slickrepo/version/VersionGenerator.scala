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

import java.time.{Instant, LocalDateTime}

import com.byteslounge.slickrepo.datetime.DateTimeHelper

/**
 * Version generator used to generate versions for
 * newly persisted or updated versioned entities.
 *
 * The version type is represented by `V` parameter
 */
trait VersionGenerator[V] {

  /**
  * Generates the initial version for the
  * version type `V`.
  */
  def initialVersion(): V

  /**
  * Generates the next version for the version
  * type `V` based on the current version which
  * is passed as an argument.
  */
  def nextVersion(currentVersion: V): V
}

/**
 * Version Generator Implicits
 */
object VersionGenerator {

  implicit val intVersionGenerator = new VersionGenerator[Int]{
    override def initialVersion(): Int = 1
    override def nextVersion(currentVersion: Int): Int = currentVersion + 1
  }

  implicit val longVersionGenerator = new VersionGenerator[Long]{
    override def initialVersion(): Long = 1
    override def nextVersion(currentVersion: Long): Long = currentVersion + 1
  }

  implicit val instantVersionGenerator = new VersionGenerator[InstantVersion]{
    override def initialVersion(): InstantVersion = InstantVersion(currentInstant())
    override def nextVersion(currentVersion: InstantVersion): InstantVersion = InstantVersion(currentInstant())
  }

  implicit val longInstantVersionGenerator = new VersionGenerator[LongInstantVersion]{
    override def initialVersion(): LongInstantVersion = LongInstantVersion(currentInstant())
    override def nextVersion(currentVersion: LongInstantVersion): LongInstantVersion = LongInstantVersion(currentInstant())
  }

  implicit val localDateTimeVersionGenerator = new VersionGenerator[LocalDateTimeVersion]{
    override def initialVersion(): LocalDateTimeVersion = LocalDateTimeVersion(currentLocalDateTime())
    override def nextVersion(currentVersion: LocalDateTimeVersion): LocalDateTimeVersion = LocalDateTimeVersion(currentLocalDateTime())
  }

  implicit val longLocalDateTimeVersionGenerator = new VersionGenerator[LongLocalDateTimeVersion]{
    override def initialVersion(): LongLocalDateTimeVersion = LongLocalDateTimeVersion(currentLocalDateTime())
    override def nextVersion(currentVersion: LongLocalDateTimeVersion): LongLocalDateTimeVersion = LongLocalDateTimeVersion(currentLocalDateTime())
  }

  /**
  * Returns an `Instant` that represents the current time.
  */
  private def currentInstant(): Instant = {
    DateTimeHelper.currentInstant
  }

  /**
  * Returns an `LocalDateTime` that represents the current LocalDateTime.
  */
  private def currentLocalDateTime(): LocalDateTime = {
    DateTimeHelper.currentLocalDateTime
  }
}
