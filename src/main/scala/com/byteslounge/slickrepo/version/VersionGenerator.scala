package com.byteslounge.slickrepo.version

trait VersionGenerator[V] {
  def initialVersion(): V
  def nextVersion(currentVersion: V): V
}
