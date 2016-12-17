package com.byteslounge.slickrepo.version

import java.util.concurrent.ConcurrentHashMap

import com.byteslounge.slickrepo.meta._

import scala.collection._
import scala.collection.convert.decorateAsScala._

import scala.reflect.runtime.universe._

class VersionHelper[T <: VersionedEntity[T, _, V], V : TypeTag] {
  def process(versionedEntity: T): T = {
    VersionedEntityTypes.process[T, V](versionedEntity)
  }
}

object VersionedEntityTypes {

  val versionedTypes: concurrent.Map[String, VersionGenerator[_]]
    = new ConcurrentHashMap[String, VersionGenerator[_]]().asScala

  {
    add(new IntVersionGenerator)
    add(new LongVersionGenerator)
    add(new InstantVersionGenerator)
  }

  def add[V : TypeTag](g: VersionGenerator[V]): Unit = {
    versionedTypes.put(typeOf[V].toString, g)
  }

  def process[T <: VersionedEntity[T, _, V], V : TypeTag](entity: T): T = {
    val generator: VersionGenerator[V] = versionedTypes(typeOf[V].toString).asInstanceOf[VersionGenerator[V]]
    entity.withVersion(entity.version.map(v => generator.nextVersion(v)).getOrElse(generator.initialVersion()))
  }
}
