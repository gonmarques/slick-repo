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
