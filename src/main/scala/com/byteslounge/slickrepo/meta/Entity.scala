/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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