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

  def findOne(id: ID): DBIO[T] = {
    tableQuery.filter(_.id === id).result.head
  }

  def save(entity: T): DBIO[ID] = {
    tableQuery returning tableQuery.map(_.id) += entity
  }

  def saveWithId(entity: T): DBIO[Int] = {
    tableQuery += entity
  }

}