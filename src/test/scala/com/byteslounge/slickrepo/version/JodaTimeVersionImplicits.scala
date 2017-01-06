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

import com.byteslounge.slickrepo.datetime.DateTimeHelper
import org.joda.time.Instant

object JodaTimeVersionImplicits {

  implicit val instantVersionGenerator = new VersionGenerator[Instant]{
    def initialVersion(): Instant = {
      currentInstant()
    }

    def nextVersion(currentVersion: Instant): Instant = {
      currentInstant()
    }

    private def currentInstant(): Instant = {
      new Instant(DateTimeHelper.currentInstant.toEpochMilli)
    }
  }

}
