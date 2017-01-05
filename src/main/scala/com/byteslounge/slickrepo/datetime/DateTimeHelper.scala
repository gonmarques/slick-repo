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

package com.byteslounge.slickrepo.datetime

import java.time.Instant

/**
 * Helper used to execute date/time related operations.
 */
object DateTimeHelper {
  var dateTimeProvider: DateTimeProvider = DateTimeProvider
  /**
  * Sets the underlying [[com.byteslounge.slickrepo.datetime.DateTimeProvider]] implementation
  * used by the helper.
  */
  def setDateTimeProvider(dateTimeProvider: DateTimeProvider): Unit = {
    this.dateTimeProvider = dateTimeProvider
  }
  /**
  * Gets the current [[java.time.Instant]].
  */
  def currentInstant: Instant = {
    dateTimeProvider.currentInstant
  }
  /**
  * Restores the underlying [[com.byteslounge.slickrepo.datetime.DateTimeProvider]]
  * used by the helper to the default implementation.
  */
  def restore(): Unit = {
    dateTimeProvider = DateTimeProvider
  }
}

/**
 * Provider used to execute date/time related operations.
 */
trait DateTimeProvider {
  /**
  * Gets the current [[java.time.Instant]].
  */
  def currentInstant: Instant
}

/**
 * Default [[com.byteslounge.slickrepo.datetime.DateTimeProvider]] implementation.
 */
private object DateTimeProvider extends DateTimeProvider {
  /**
  * Gets the current [[java.time.Instant]].
  */
  override def currentInstant: Instant = Instant.now()
}
