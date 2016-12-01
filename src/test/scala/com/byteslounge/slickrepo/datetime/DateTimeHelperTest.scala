package com.byteslounge.slickrepo.datetime

import java.time.Instant

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class DateTimeHelperTest extends FlatSpec with Matchers with BeforeAndAfter {

  before {
    MockDateTimeHelper.restore()
  }

  "The DateTimeHelper" should "return the current instant" in {
    val now: Instant = Instant.now()
    val currentInstant: Instant = DateTimeHelper.currentInstant
    currentInstant.toEpochMilli should be >= now.toEpochMilli
  }
}
