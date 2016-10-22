package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile
import slick.profile.RelationalProfile

import scala.concurrent.ExecutionContext

abstract class Repository[T <: Entity[T, ID], ID](val driver: JdbcProfile) {

  import driver.api._

  type TableType <: Keyed[ID] with RelationalProfile#Table[T]
  def pkType: BaseTypedType[ID]
  implicit lazy val _pkType: BaseTypedType[ID] = pkType
  def tableQuery: TableQuery[TableType]

  def findAll(): DBIO[Seq[T]] = {
    tableQueryCompiled.result
  }

  def findOne(id: ID): DBIO[T] = {
    findOneCompiled(id).result.head
  }

  def lock(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val result = findOneCompiled(entity.id.get).result
    result.overrideStatements(
      Seq(exclusiveLockStatement(result.statements.head))
    ).map(_ => entity)
  }

  def searchOne(id: ID): DBIO[Option[T]] = {
    findOneCompiled(id).result.headOption
  }

  def save(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    (saveCompiled += entity).map(id => entity.withId(id))
  }

  def saveWithId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    (tableQueryCompiled += entity).map(_ => entity)
  }

  def update(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    findOneCompiled(entity.id.get).update(entity).map(_ => entity)
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

  private def exclusiveLockStatement(sql: String): String = {
    driver.getClass.getSimpleName.toLowerCase match {
      case n: String if n.contains("db2") => sql + " FOR UPDATE WITH RS"
      case n: String if n.contains("sqlserver") => sql.replaceFirst(" where ", " WITH (UPDLOCK, ROWLOCK) WHERE ")
      case _: String => sql + " FOR UPDATE"
    }
  }

  lazy protected val tableQueryCompiled = Compiled(tableQuery)
  lazy protected val findOneCompiled = Compiled((id: Rep[ID]) => tableQuery.filter(_.id === id))
  lazy protected val saveCompiled = tableQuery returning tableQuery.map(_.id)
  lazy private val countCompiled = Compiled(tableQuery.map(_.id).length)

}
