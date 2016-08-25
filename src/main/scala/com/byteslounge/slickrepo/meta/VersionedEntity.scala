package com.byteslounge.slickrepo.meta

abstract class VersionedEntity[T <: VersionedEntity[T, ID, V], ID, V](val version: Option[V] = None) extends Entity[T, ID] {

  def withVersion(version: V): T

}
