package com.byteslounge.slickrepo.version

import com.byteslounge.slickrepo.datetime.DateTimeHelper
import org.joda.time.Instant

class JodaTimeVersionGenerator extends VersionGenerator[Instant] {

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
