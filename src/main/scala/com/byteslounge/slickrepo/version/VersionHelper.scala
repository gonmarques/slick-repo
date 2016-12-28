package com.byteslounge.slickrepo.version

import java.util.concurrent.ConcurrentHashMap

import com.byteslounge.slickrepo.exception.VersionGeneratorNotFoundException
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
    val versionType: String = typeOf[V].toString
    versionedTypes.get(versionType) match {
      case Some(rawGenerator) =>
        val generator: VersionGenerator[V] = rawGenerator.asInstanceOf[VersionGenerator[V]]
        entity.withVersion(entity.version.map(v => generator.nextVersion(v)).getOrElse(generator.initialVersion()))
      case None => throw new VersionGeneratorNotFoundException(missingGeneratorMessage(versionType))
    }
  }

  private def missingGeneratorMessage(versionType: String): String = {
    val versionGeneratorClassName = classOf[VersionGenerator[_]].getSimpleName
    val versionHelperClassName = classOf[VersionHelper[_, _]].getSimpleName
    s"Could not find a $versionGeneratorClassName for version field of type: $versionType." +
      s" A VersionGenerator for type $versionType should be implemented and registered via $versionHelperClassName#add()"
  }
}
