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

  implicit val instantVersionGenerator = new VersionGenerator[Instant]{
    override def initialVersion(): Instant = currentInstant()
    override def nextVersion(currentVersion: Instant): Instant = currentInstant()
  }

  /**
   * Returns an `Instant` that represents the current time.
   */
  private def currentInstant(): Instant = {
    DateTimeHelper.currentInstant
  }

}
