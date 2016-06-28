package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.Keyed
import slick.driver.JdbcProfile
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.meta.Entity

abstract class Repository[T <: Entity[ID], ID, K <: Keyed[ID]](val driver: JdbcProfile) {

  import driver.api._

  def pkType: BaseTypedType[ID]
  implicit lazy val _pkType: BaseTypedType[ID] = pkType

  type TableType <: Table[T] with K
  def tableQuery: TableQuery[TableType]

  def findOne(id: ID): DBIO[T] = {
    tableQuery.filter(_.id === id).result.head
  }

  def save(entity: T): DBIO[ID] = {
    tableQuery returning tableQuery.map(_.id) += entity
  }

}