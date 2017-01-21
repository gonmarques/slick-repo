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
