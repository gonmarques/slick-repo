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

import java.time.Instant

import com.byteslounge.slickrepo.datetime.DateTimeHelper

abstract class VersionedEntity[T <: VersionedEntity[T, ID, V], ID, V](val version: Option[V] = None)(implicit versionGenerator : VersionGenerator[V]) extends Entity[T, ID] {
  def withVersion(version: V): T
  def withNewVersion(version: Option[V]): T = withVersion(version.fold(versionGenerator.init()){ v =>
    versionGenerator.next(v)})
}



object VersionGeneratorImplicits {

  implicit val intGenerator = new VersionGenerator[Int]{
    override def init(): Int = 1
    override def next(current: Int): Int = current + 1
  }

  implicit val longGenerator = new VersionGenerator[Long]{
    override def init(): Long = 1
    override def next(current: Long): Long = current + 1
  }

  implicit val instantGenerator = new VersionGenerator[Instant]{
    override def init(): Instant = currentInstant()
    override def next(current: Instant): Instant = currentInstant()
  }

   private def currentInstant(): Instant = {
      DateTimeHelper.currentInstant
   }

}