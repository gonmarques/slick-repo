package com.byteslounge.slickrepo.meta

import java.time.Instant

import com.byteslounge.slickrepo.datetime.DateTimeHelper

abstract class VersionedEntity[T <: VersionedEntity[T, ID, V], ID, V](val version: Option[V] = None)(implicit nextVersion : (V) => Version[V], initialVersion : () => Version[V]) extends Entity[T, ID] {
  def withVersion(version: V): T
  def withNewVersion(version: Option[V]): T = withVersion(version.fold(initialVersion()){ v =>
    nextVersion(v)}.current)
}



object VersionEntityImplicits {

  implicit def initialVersionInt(): Version[Int] = Version(1)
  implicit def nextVersionInt(current : Int): Version[Int] = {
    Version(current + 1)
  }

  implicit def initialVersionLong(): Version[Long] = Version(1L)
  implicit def nextVersionLong(current : Long): Version[Long] = {
    Version(current + 1L)
  }

  implicit def initialVersionInstant(): Version[Instant] =  Version(currentInstant())
  implicit def nextVersionInstant(currentVersion: Instant): Version[Instant] = Version(currentInstant())

  private def currentInstant(): Instant = {
    DateTimeHelper.currentInstant
  }
}