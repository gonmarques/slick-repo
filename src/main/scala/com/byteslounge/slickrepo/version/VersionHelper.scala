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

package com.byteslounge.slickrepo.version

import java.util.concurrent.ConcurrentHashMap

import com.byteslounge.slickrepo.exception.VersionGeneratorNotFoundException
import com.byteslounge.slickrepo.meta._

import scala.collection._
import scala.collection.convert.decorateAsScala._
import scala.reflect.runtime.universe._

/**
 * Helper that aggregates version related operations.
 */
class VersionHelper[T <: VersionedEntity[T, _, V], V : TypeTag] {

  /**
  * Processes the versioned entity and returns a new entity
  * with the version field updated accordingly.
  *
  * If the entity is a new entity that was never persisted,
  * and its version field is still undefined, then the initial
  * version will be assigned to the returned entity.
  *
  * If the entity already exists in the database and is to
  * be updated, then its version field will be updated to
  * the next version.
  */
  def process(versionedEntity: T): T = {
    VersionedEntityTypes.process[T, V](versionedEntity)
  }
}

/**
 * Registry of entity version generators.
 */
object VersionedEntityTypes {

  val versionedTypes: concurrent.Map[String, VersionGenerator[_]]
    = new ConcurrentHashMap[String, VersionGenerator[_]]().asScala

  {
    add(new IntVersionGenerator)
    add(new LongVersionGenerator)
    add(new InstantVersionGenerator)
  }

  /**
  * Registers a new version generator for a version type `V`.
  */
  def add[V : TypeTag](g: VersionGenerator[V]): Unit = {
    versionedTypes.put(typeOf[V].toString, g)
  }

  /**
  * Processes the entity passed as an argument and returns a
  * new entity with the version field updated.
  */
  def process[T <: VersionedEntity[T, _, V], V : TypeTag](entity: T): T = {
    val versionType: String = typeOf[V].toString
    versionedTypes.get(versionType) match {
      case Some(rawGenerator) =>
        val generator: VersionGenerator[V] = rawGenerator.asInstanceOf[VersionGenerator[V]]
        entity.withVersion(entity.version.map(v => generator.nextVersion(v)).getOrElse(generator.initialVersion()))
      case None => throw new VersionGeneratorNotFoundException(missingGeneratorMessage(versionType))
    }
  }

  /**
  * Generates the error message used when the application requests
  * a version generator for a type which generator was not registered.
  */
  private def missingGeneratorMessage(versionType: String): String = {
    val versionGeneratorClassName = classOf[VersionGenerator[_]].getSimpleName
    val versionHelperClassName = classOf[VersionHelper[_, _]].getSimpleName
    s"Could not find a $versionGeneratorClassName for version field of type: $versionType." +
      s" A VersionGenerator for type $versionType should be implemented and registered via $versionHelperClassName#add()"
  }
}
