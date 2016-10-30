package com.byteslounge.slickrepo.meta

abstract class LongVersionedEntity[T <: VersionedEntity[T, ID, Long], ID](override val version: Option[Long] = None) extends VersionedEntity[T, ID, Long] {
}
