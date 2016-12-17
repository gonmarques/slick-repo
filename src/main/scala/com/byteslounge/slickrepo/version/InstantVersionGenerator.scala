package com.byteslounge.slickrepo.version

import java.time.Instant

import com.byteslounge.slickrepo.datetime.DateTimeHelper

class InstantVersionGenerator extends VersionGenerator[Instant] {

  def initialVersion(): Instant = {
    currentInstant()
  }

  def nextVersion(currentVersion: Instant): Instant = {
    currentInstant()
  }

  private def currentInstant(): Instant = {
    DateTimeHelper.currentInstant
  }
}
