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

/**
 * Version generator used to generate versions
 * for newly persisted or updated versioned entities
 * which version field is of type `Long`.
 */
class LongVersionGenerator extends VersionGenerator[Long] {

  /**
  * Initial version for a version field of type `Long`.
  *
  * The initial value is 1.
  */
  def initialVersion(): Long = {
    1
  }

  /**
  * Next version for version field type `Long` based
  * on a current version.
  *
  * The next version is the current version
  * incremented by 1.
  */
  def nextVersion(currentVersion: Long): Long = {
    currentVersion + 1
  }
}
