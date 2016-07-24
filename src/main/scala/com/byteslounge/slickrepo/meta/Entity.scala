package com.byteslounge.slickrepo.meta

import com.byteslounge.slickrepo.utils.ClassUtils.sameClass

abstract class Entity[T <: Entity[T, ID], ID](val id: Option[ID] = None) {
  
  def withId(id: ID): T

  override def equals(obj: Any): Boolean = {
    obj match {
      case entity: Entity[_, _] if sameClass(this, entity)
        && this.id.nonEmpty && entity.id.nonEmpty
        && this.id.get == entity.id.get => true
      case _ => false
    }
  }

  override def hashCode() = {
    id match {
      case Some(id) => id.hashCode
      case _        => 1
    }
  }

}