package com.byteslounge.slickrepo.meta

import java.time.Instant

abstract class InstantVersionedEntity[T <: VersionedEntity[T, ID, Instant], ID](override val version: Option[Instant] = None) extends VersionedEntity[T, ID, Instant] {
}
