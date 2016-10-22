package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.exception.OptimisticLockException
import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.version.VersionHelper
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile
import slick.profile.RelationalProfile

import scala.concurrent.ExecutionContext

abstract class VersionedRepository[T <: VersionedEntity[T, ID, V], ID, V] (override val driver: JdbcProfile) extends Repository[T, ID](driver) {

  import driver.api._

  def versionType: BaseTypedType[V]
  implicit lazy val _versionType: BaseTypedType[V] = versionType
  type TableType <: Versioned[ID, V] with RelationalProfile#Table[T]

  override def save(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    (saveCompiled += versionedEntity).map(id => versionedEntity.withId(id))
  }

  override def saveWithId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    (tableQueryCompiled += versionedEntity).map(_ => versionedEntity)
  }

  override def update(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val versionedEntity = applyVersion(entity)
    findOneVersionedCompiled(versionedEntity.id.get, entity.version.get).update(versionedEntity)
      .map(updateCheck(versionedEntity, entity.version.get))
  }

  private def applyVersion(entity: T): T = {
    new VersionHelper[T].process(entity)
  }

  private def updateCheck(updatedEntity: T, expectedVersion: Any): (Int => T) = {
    (count) => {
      count match {
        case 1 => updatedEntity
        case _ => throw new OptimisticLockException("Failed to update entity of type " + updatedEntity.getClass.getName + ". Expected version was not found: " + expectedVersion)
      }
    }
  }

  lazy private val findOneVersionedCompiled = Compiled((id: Rep[ID], version: Rep[V]) => tableQuery.filter(_.id === id).filter(_.version === version))

}
