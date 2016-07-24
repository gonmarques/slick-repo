package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.Keyed
import slick.driver.JdbcProfile
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.meta.Entity
import slick.profile.RelationalProfile

abstract class Repository[T <: Entity[ID], ID, K <: Keyed[ID] with RelationalProfile#Table[T]](val driver: JdbcProfile) {

  import driver.api._

  def pkType: BaseTypedType[ID]
  implicit lazy val _pkType: BaseTypedType[ID] = pkType
  def tableQuery: TableQuery[K]

  def findAll(): DBIO[Seq[T]] = {
    tableQueryCompiled.result
  }

  def findOne(id: ID): DBIO[T] = {
    findOneCompiled(id).result.head
  }

  def searchOne(id: ID): DBIO[Option[T]] = {
    findOneCompiled(id).result.headOption
  }

  def save(entity: T): DBIO[ID] = {
    saveCompiled += entity
  }

  def saveWithId(entity: T): DBIO[Int] = {
    tableQueryCompiled += entity
  }

  def update(entity: T): DBIO[Int] = {
    findOneCompiled(entity.id.get).update(entity)
  }

  def delete(id: ID): DBIO[Int] = {
    findOneCompiled(id).delete
  }

  def count(): DBIO[Int] = {
    countCompiled.result
  }

  def executeTransactionally[R](work: DBIO[R]): DBIO[R] = {
    work.transactionally
  }
  
  lazy private val tableQueryCompiled = Compiled(tableQuery)
  lazy private val findOneCompiled = Compiled((id: Rep[ID]) => tableQuery.filter(_.id === id))
  lazy private val saveCompiled = tableQuery returning tableQuery.map(_.id)
  lazy private val countCompiled = Compiled(tableQuery.map(_.id).length)

}
