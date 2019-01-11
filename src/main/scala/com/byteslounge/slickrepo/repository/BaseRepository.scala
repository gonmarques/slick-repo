/*
 * MIT License
 *
 * Copyright (c) 2016 Gonçalo Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.byteslounge.slickrepo.repository

import com.byteslounge.slickrepo.annotation._
import com.byteslounge.slickrepo.meta.Entity
import com.byteslounge.slickrepo.scalaversion.{JdbcProfile, RelationalProfile}
import slick.lifted.AppliedCompiledFunction

import scala.annotation.StaticAnnotation
import scala.concurrent.ExecutionContext

/**
  * Repository used to execute CRUD operations against a database for
  * a given entity type.
  */
trait BaseRepository[T <: Entity[T, ID], ID] {

  protected val driver: JdbcProfile

  import driver.api._

  type TableType <: RelationalProfile#Table[T]
  def tableQuery: TableQuery[TableType]

  type F = AppliedCompiledFunction[_, Query[TableType, T, Seq], Seq[T]]

  protected def findOneQuery(id: ID): F

  /**
    * Finds all entities.
    */
  def findAll()(implicit ec: ExecutionContext): DBIO[Seq[T]] = {
    tableQueryCompiled.result.map(seq => sequenceLifecycleEvent(seq, _postLoad, classOf[postLoad]))
  }

  /**
    * Finds a given entity by its primary key.
    */
  def findOne(id: ID)(implicit ec: ExecutionContext): DBIO[Option[T]] = {
    findOneQuery(id).result.headOption.map(e => e.map(_postLoad))
  }

  /**
    * Locks an entity using a pessimistic lock.
    */
  def lock(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val result = findOneQuery(entity.id.get).result
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
  def save(entity: T)(implicit ec: ExecutionContext): DBIO[T] =
    entity.id match {
      case None    => generatedIdPersister(entity, ec)
      case Some(_) => predefinedIdPersister(entity, ec)
    }

  /**
    * Generated ID persister
    */
  protected val generatedIdPersister: (T, ExecutionContext) => DBIO[T] =
    getGeneratedIdPersister(identity)

  protected[this] def saveQuery: driver.IntoInsertActionComposer[T, ID]

  /**
    * Builds a generated ID persister
    */
  protected def getGeneratedIdPersister(transformer: T => T): (T, ExecutionContext) => DBIO[T] =
    (entity: T, ec: ExecutionContext) => {
      val transformed = transformer(_prePersist(entity))
      (saveQuery += transformed).map(id => _postPersist(transformed.withId(id)))(ec)
    }

  /**
    * Predefined ID persister
    */
  protected val predefinedIdPersister: (T, ExecutionContext) => DBIO[T] =
    getPredefinedIdPersister(identity)

  /**
    * Builds a predefined ID persister
    */
  protected def getPredefinedIdPersister(transformer: T => T): (T, ExecutionContext) => DBIO[T] =
    (entity: T, ec: ExecutionContext) => {
      val transformed = transformer(_prePersist(entity))
      (tableQueryCompiled += transformed).map(_ => _postPersist(transformed))(ec)
    }

  /**
    * Performs a batch insert of the entities that are passed in
    * as an argument. The result will be the number of created
    * entities in case of a successful batch insert execution
    * (if the row count is provided by the underlying database
    * or driver. If not, then `None` will be returned as the
    * result of a successful batch insert operation).
    */
  def batchInsert(entities: Seq[T]): DBIO[Option[Int]] =
    batchPersister(entities)

  /**
    * Batch persister
    */
  protected val batchPersister: Seq[T] => DBIO[Option[Int]] =
    getBatchPersister(seq => sequenceLifecycleEvent(seq, _prePersist, classOf[prePersist]))

  /**
    * Builds a batch persister
    */
  protected def getBatchPersister(seqTransformer: Seq[T] => Seq[T]): Seq[T] => DBIO[Option[Int]] =
    (entities: Seq[T]) => tableQueryCompiled ++= seqTransformer(entities)

  /**
    * Updates a given entity in the database.
    *
    * If the entity is not yet persisted in the database then
    * this operation will result in an exception being thrown.
    *
    * Returns the same entity instance that was passed in as
    * an argument.
    */
  def update(entity: T)(implicit ec: ExecutionContext): DBIO[T] =
    updater(entity, updateFinder(entity), ec)

  /**
    * Update validator
    */
  protected def updateValidator(previous: T, next: T): Int => T = _ => next

  /**
    * Update finder
    */
  protected def updateFinder(entity: T): F =
    findOneQuery(entity.id.get)

  /**
    * Updater
    */
  protected val updater: (T, F, ExecutionContext) => DBIO[T] =
    getUpdater(identity)

  /**
    * Builds an updater
    */
  protected def getUpdater(transformer: T => T): (T, F, ExecutionContext) => DBIO[T] =
    (entity: T, finder: F, ec: ExecutionContext) => {
      val transformed = transformer(_preUpdate(entity))
      finder.update(transformed).map(_postUpdate compose updateValidator(entity, transformed))(ec)
    }

  /**
    * Deletes a given entity from the database.
    *
    * If the entity is not yet persisted in the database then
    * this operation will result in an exception being thrown.
    */
  def delete(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val preDeleted = _preDelete(entity)
    findOneQuery(entity.id.get).delete.map(_ => _postDelete(preDeleted))
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

  private def sequenceLifecycleEvent(seq: Seq[T], handler: T => T, handlerType: Class[_ <: StaticAnnotation]): Seq[T] = {
    if (LifecycleHelper.isLifecycleHandlerDefined(this.getClass, handlerType)) {
      seq.map(handler)
    } else {
      seq
    }
  }

  lazy protected val tableQueryCompiled = Compiled(tableQuery)
  lazy private val countCompiled = Compiled(tableQuery.length)

  private val _postLoad: (T => T) = createHandler(classOf[postLoad])
  private val _prePersist: (T => T) = createHandler(classOf[prePersist])
  private val _postPersist: (T => T) = createHandler(classOf[postPersist])
  private val _preUpdate: (T => T) = createHandler(classOf[preUpdate])
  private val _postUpdate: (T => T) = createHandler(classOf[postUpdate])
  private val _preDelete: (T => T) = createHandler(classOf[preDelete])
  private val _postDelete: (T => T) = createHandler(classOf[postDelete])

  protected def _getPrePersist: (T => T) = _prePersist

  private def createHandler(handlerType: Class[_ <: StaticAnnotation]) = {
    LifecycleHelper.createLifecycleHandler[T, ID](this, handlerType)
  }
}
