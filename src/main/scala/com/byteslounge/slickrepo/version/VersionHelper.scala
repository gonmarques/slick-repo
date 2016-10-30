package com.byteslounge.slickrepo.version

import com.byteslounge.slickrepo.meta.{Entity, IntVersionedEntity, LongVersionedEntity}

class VersionHelper[T <: Entity[T, _]] {
  def process(entity: T): T = {
    entity match {
      case ive: IntVersionedEntity[T, _] => ive.withVersion(ive.version.map(v => v + 1).getOrElse(1))
      case lve: LongVersionedEntity[T, _] => lve.withVersion(lve.version.map(v => v + 1).getOrElse(1))
      case _                             => throw new IllegalStateException("Versioned Entity version type is not supported: " + entity.getClass.getName)
    }
  }
}
