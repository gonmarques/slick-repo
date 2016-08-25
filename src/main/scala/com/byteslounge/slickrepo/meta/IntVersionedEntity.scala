package com.byteslounge.slickrepo.meta

import com.byteslounge.slickrepo.version.VersionHelper

abstract class IntVersionedEntity[T <: VersionedEntity[T, ID, Int], ID](override val version: Option[Int] = None) extends VersionedEntity[T, ID, Int] {
}
