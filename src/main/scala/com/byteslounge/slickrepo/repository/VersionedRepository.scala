package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.version.VersionHelper
import slick.driver.JdbcProfile
import slick.profile.RelationalProfile

import scala.concurrent.ExecutionContext

abstract class VersionedRepository[T <: VersionedEntity[T, ID, V], ID, K <: Versioned[ID, V] with RelationalProfile#Table[T], V] (override val driver: JdbcProfile) extends Repository[T, ID, K](driver) {

  import driver.api._

  override def save(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    (saveCompiled += versionedEntity).map(id => versionedEntity.withId(id))
  }

  override def update(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    findOneCompiled(versionedEntity.id.get).update(versionedEntity).map(_ => versionedEntity)
  }

  private def applyVersion(entity: T): T = {
    new VersionHelper[T].process(entity)
  }

}
