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

object MockDateTimeHelper {
  def start(): Unit = {
    MockDateTimeProvider.reset()
    DateTimeHelper.setDateTimeProvider(MockDateTimeProvider)
  }
  def mock(instants: Instant*): Unit = {
    MockDateTimeProvider.mock(instants)
  }
  def restore(): Unit = {
    DateTimeHelper.restore()
  }
}

private object MockDateTimeProvider extends DateTimeProvider {
  var instants: Seq[Instant] = _
  def reset(): Unit = {
    instants = Seq()
  }
  override def currentInstant: Instant = {
    val result: Instant = instants.head
    instants = instants.tail
    result
  }
  def mock(instants: Seq[Instant]): Unit = {
    instants.foreach(instant => this.instants = this.instants :+ instant)
  }
}
