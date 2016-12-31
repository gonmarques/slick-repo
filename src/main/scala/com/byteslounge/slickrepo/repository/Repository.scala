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
    entity.id match {
      case None    => saveUsingGeneratedId(entity)
      case Some(_) => saveUsingPredefinedId(entity)
    }
  }

  private def saveUsingGeneratedId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    (saveCompiled += entity).map(id => entity.withId(id))
  }

  private def saveUsingPredefinedId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
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
      case n: String if n.contains("db2") || n.contains("derby") => sql + " FOR UPDATE WITH RS"
      case n: String if n.contains("sqlserver") => sql.replaceFirst(" where ", " WITH (UPDLOCK, ROWLOCK) WHERE ")
      case _: String => sql + " FOR UPDATE"
    }
  }

  lazy protected val tableQueryCompiled = Compiled(tableQuery)
  lazy protected val findOneCompiled = Compiled((id: Rep[ID]) => tableQuery.filter(_.id === id))
  lazy protected val saveCompiled = tableQuery returning tableQuery.map(_.id)
  lazy private val countCompiled = Compiled(tableQuery.map(_.id).length)

}
