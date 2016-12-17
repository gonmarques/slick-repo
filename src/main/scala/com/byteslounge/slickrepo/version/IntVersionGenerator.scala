package com.byteslounge.slickrepo.version

class IntVersionGenerator extends VersionGenerator[Int] {

  def initialVersion(): Int = {
    1
  }

  def nextVersion(currentVersion: Int): Int = {
    currentVersion + 1
  }
}
