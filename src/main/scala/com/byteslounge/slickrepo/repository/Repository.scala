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
import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import com.byteslounge.slickrepo.scalaversion.RelationalProfile

import scala.concurrent.ExecutionContext

/**
 * Repository used to execute CRUD operations against a database for
 * a given entity type.
 */
abstract class Repository[T <: Entity[T, ID], ID](val driver: JdbcProfile) {

  import driver.api._

  type TableType <: Keyed[ID] with RelationalProfile#Table[T]
  def pkType: BaseTypedType[ID]
  implicit lazy val _pkType: BaseTypedType[ID] = pkType
  def tableQuery: TableQuery[TableType]

  /**
  * Finds all entities.
  */
  def findAll(): DBIO[Seq[T]] = {
    tableQueryCompiled.result
  }

  /**
  * Finds a given entity by its primary key.
  */
  def findOne(id: ID): DBIO[Option[T]] = {
    findOneCompiled(id).result.headOption
  }

  /**
  * Locks an entity using a pessimistic lock.
  */
  def lock(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val result = findOneCompiled(entity.id.get).result
    result.overrideStatements(
      Seq(exclusiveLockStatement(result.statements.head))
    ).map(_ => entity)
  }

  /**
  * Persists an entity for the first time.
  *
  * If the entity has an already assigned primary key, then it will
  * be persisted with that same primary key.
  *
  * If the entity doesn't have an already assigned primary key, then
  * it will be persisted using an auto-generated primary key using
  * the generation strategy configured in the entity definition.
  *
  * A new entity with the primary key assigned to it will be returned.
  */
  def save(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    entity.id match {
      case None    => saveUsingGeneratedId(entity)
      case Some(_) => saveUsingPredefinedId(entity)
    }
  }

  /**
  * Persists an entity using an auto-generated primary key.
  */
  private def saveUsingGeneratedId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    (saveCompiled += entity).map(id => entity.withId(id))
  }

  /**
  * Persists an entity using a predefined primary key.
  */
  private def saveUsingPredefinedId(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    (tableQueryCompiled += entity).map(_ => entity)
  }

  /**
  * Updates a given entity in the database.
  *
  * If the entity is not yet persisted in the database then
  * this operation will result in an exception being thrown.
  *
  * Returns the same entity instance that was passed in as
  * an argument.
  */
  def update(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    findOneCompiled(entity.id.get).update(entity).map(_ => entity)
  }

  /**
  * Deletes a given entity from the database.
  *
  * If the entity is not yet persisted in the database then
  * this operation will result in an exception being thrown.
  */
  def delete(id: ID): DBIO[Int] = {
    findOneCompiled(id).delete
  }

  /**
  * Counts all entities.
  */
  def count(): DBIO[Int] = {
    countCompiled.result
  }

  /**
  * Executes the given unit of work in a single transaction.
  */
  def executeTransactionally[R](work: DBIO[R]): DBIO[R] = {
    work.transactionally
  }

  /**
  * Returns the pessimistic lock statement based on the
  * current database driver type.
  */
  def exclusiveLockStatement(sql: String): String = {
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
