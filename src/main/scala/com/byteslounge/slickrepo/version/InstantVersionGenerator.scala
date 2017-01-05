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
 * Version generator used to generate versions
 * for newly persisted or updated versioned entities
 * which version field is of type `Instant`.
 */
class InstantVersionGenerator extends VersionGenerator[Instant] {

  /**
  * Initial version for a version field of type `Instant`.
  *
  * The initial value the current time.
  */
  def initialVersion(): Instant = {
    currentInstant()
  }

  /**
  * Next version for version field type `Instant`.
  *
  * The next version is the current time.
  */
  def nextVersion(currentVersion: Instant): Instant = {
    currentInstant()
  }

  /**
  * Returns an `Instant` that represents the current time.
  */
  private def currentInstant(): Instant = {
    DateTimeHelper.currentInstant
  }
}
