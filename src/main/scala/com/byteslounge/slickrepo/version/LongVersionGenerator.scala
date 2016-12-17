package com.byteslounge.slickrepo.version

class LongVersionGenerator extends VersionGenerator[Long] {

  def initialVersion(): Long = {
    1
  }

  def nextVersion(currentVersion: Long): Long = {
    currentVersion + 1
  }
}
