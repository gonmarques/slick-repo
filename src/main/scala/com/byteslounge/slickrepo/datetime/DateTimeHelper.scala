package com.byteslounge.slickrepo.datetime

import java.time.Instant

object DateTimeHelper {
  var dateTimeProvider: DateTimeProvider = DateTimeProvider
  def setDateTimeProvider(dateTimeProvider: DateTimeProvider): Unit = {
    this.dateTimeProvider = dateTimeProvider
  }
  def currentInstant: Instant = {
    dateTimeProvider.currentInstant
  }
  def restore(): Unit = {
    dateTimeProvider = DateTimeProvider
  }
}

trait DateTimeProvider {
  def currentInstant: Instant
}

private object DateTimeProvider extends DateTimeProvider {
  override def currentInstant: Instant = Instant.now()
}
