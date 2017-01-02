package com.byteslounge.slickrepo.meta

import java.time.Instant

import com.byteslounge.slickrepo.datetime.DateTimeHelper

abstract class VersionedEntity[T <: VersionedEntity[T, ID, V], ID, V : VersionGenerator](val version: Option[V] = None) extends Entity[T, ID] {
  val generator = implicitly[VersionGenerator[V]]
  def withVersion(version: V): T
  def withNewVersion(version: Option[V]): T = withVersion(version.fold(generator.init()){ v =>
    generator.next(v)})
}