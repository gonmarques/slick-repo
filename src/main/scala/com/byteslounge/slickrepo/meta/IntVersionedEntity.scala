package com.byteslounge.slickrepo.meta

abstract class IntVersionedEntity[T <: VersionedEntity[T, ID, Int], ID](override val version: Option[Int] = None) extends VersionedEntity[T, ID, Int] {
}
