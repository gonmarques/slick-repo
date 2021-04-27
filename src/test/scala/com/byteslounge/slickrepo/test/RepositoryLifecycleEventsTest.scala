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

package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.annotation._
import com.byteslounge.slickrepo.exception.{DuplicatedHandlerException, ListenerInvocationException}
import com.byteslounge.slickrepo.meta.{Entity, Keyed, Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository._
import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import slick.ast.BaseTypedType

abstract class RepositoryLifecycleEventsTest(override val config: Config) extends AbstractRepositoryTest(config) {

  "The Repository Lifecycle events" should "trigger a postLoad event - non versioned entity - findOne" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: LifecycleEntity = executeAction(lifecycleEntityRepositoryPostLoad.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read: LifecycleEntity = executeAction(lifecycleEntityRepositoryPostLoad.findOne(entity.id.get)).get
    read.name should equal("postLoad")
  }

  it should "trigger a postLoad event - versioned entity - findOne" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity: LifecycleVersionedEntity = executeAction(lifecycleVersionedEntityRepositoryPostLoad.save(LifecycleVersionedEntity(None, "john", "f1", None)))
    val read: LifecycleVersionedEntity = executeAction(lifecycleVersionedEntityRepositoryPostLoad.findOne(entity.id.get)).get
    read.name should equal("postLoad")
  }

  it should "trigger a postLoad event - non versioned entity - findAll" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    executeAction(lifecycleEntityRepositoryPostLoad.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read: Seq[LifecycleEntity] = executeAction(lifecycleEntityRepositoryPostLoad.findAll())
    read.head.name should equal("postLoad")
  }

  it should "trigger a postLoad event - versioned entity - findAll" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    executeAction(lifecycleVersionedEntityRepositoryPostLoad.save(LifecycleVersionedEntity(None, "john", "f1", None)))
    val read: Seq[LifecycleVersionedEntity] = executeAction(lifecycleVersionedEntityRepositoryPostLoad.findAll())
    read.head.name should equal("postLoad")
  }

  it should "trigger a prePersist event - non versioned entity + auto PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryPrePersistAutoPk.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read = executeAction(lifecycleEntityRepositoryPrePersistAutoPk.findOne(entity.id.get)).get
    read.name should equal("prePersist")
  }

  it should "trigger a prePersist event - non versioned entity + manual PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryManualPk.save(LifecycleEntityManualPk(Some(7), "john", "f1", "f2", "f3", "f4")))
    val read = executeAction(lifecycleEntityRepositoryManualPk.findOne(entity.id.get)).get
    read.name should equal("prePersist")
  }

  it should "trigger a prePersist event - non versioned entity + auto PK - batchInsert" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    executeAction(lifecycleEntityRepositoryPrePersistAutoPk.batchInsert(Seq(LifecycleEntity(None, "john", "f1", "f2"), LifecycleEntity(None, "john", "f1", "f2"))))
    executeAction(lifecycleEntityRepositoryPrePersistAutoPk.findOne(1)).get.name should equal("prePersist")
    executeAction(lifecycleEntityRepositoryPrePersistAutoPk.findOne(2)).get.name should equal("prePersist")
  }

  it should "trigger a prePersist event - non versioned entity + manual PK - batchInsert" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    executeAction(lifecycleEntityRepositoryManualPk.batchInsert(Seq(LifecycleEntityManualPk(Some(7), "john", "f1", "f2", "f3", "f4"), LifecycleEntityManualPk(Some(8), "john2", "f5", "f6", "f7", "f8"))))
    executeAction(lifecycleEntityRepositoryManualPk.findOne(7)).get.name should equal("prePersist")
    executeAction(lifecycleEntityRepositoryManualPk.findOne(8)).get.name should equal("prePersist")
  }

  it should "trigger a prePersist event - versioned entity + auto PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleVersionedEntityRepositoryPrePersistAutoPk.save(LifecycleVersionedEntity(None, "john", "f1", None)))
    val read = executeAction(lifecycleVersionedEntityRepositoryPrePersistAutoPk.findOne(entity.id.get)).get
    read.name should equal("prePersist")
  }

  it should "trigger a prePersist event - versioned entity + manual PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleVersionedEntityRepositoryManualPk.save(LifecycleVersionedEntityManualPk(Some(8), "john", "f1", None)))
    val read = executeAction(lifecycleVersionedEntityRepositoryManualPk.findOne(entity.id.get)).get
    read.name should equal("prePersist")
  }

  it should "trigger a prePersist event - versioned entity + auto PK - batchInsert" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    executeAction(lifecycleVersionedEntityRepositoryPrePersistAutoPk.batchInsert(Seq(LifecycleVersionedEntity(None, "john", "f1", None), LifecycleVersionedEntity(None, "john", "f1", None))))
    executeAction(lifecycleVersionedEntityRepositoryPrePersistAutoPk.findOne(1)).get.name should equal("prePersist")
  }

  it should "trigger a prePersist event - versioned entity + manual PK - batchInsert" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    executeAction(lifecycleVersionedEntityRepositoryManualPk.batchInsert(Seq(LifecycleVersionedEntityManualPk(Some(7), "john", "f1", None), LifecycleVersionedEntityManualPk(Some(8), "john", "f1", None))))
    executeAction(lifecycleVersionedEntityRepositoryManualPk.findOne(7)).get.name should equal("prePersist")
    executeAction(lifecycleVersionedEntityRepositoryManualPk.findOne(8)).get.name should equal("prePersist")
  }

  it should "trigger a postPersist event - non versioned entity + auto PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryPrePersistAutoPk.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read = executeAction(lifecycleEntityRepositoryPrePersistAutoPk.findOne(entity.id.get)).get
    read.field1 should equal("f1")
    entity.field1 should equal("postPersist")
  }

  it should "trigger a postPersist event - non versioned entity + manual PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryManualPk.save(LifecycleEntityManualPk(Some(7), "john", "f1", "f2", "f3", "f4")))
    val read = executeAction(lifecycleEntityRepositoryManualPk.findOne(entity.id.get)).get
    read.field1 should equal("f1")
    entity.field1 should equal("postPersist")
  }

  it should "trigger a postPersist event - versioned entity + auto PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleVersionedEntityRepositoryPrePersistAutoPk.save(LifecycleVersionedEntity(None, "john", "f1", None)))
    val read = executeAction(lifecycleVersionedEntityRepositoryPrePersistAutoPk.findOne(entity.id.get)).get
    read.field1 should equal("f1")
    entity.field1 should equal("postPersist")
  }

  it should "trigger a postPersist event - versioned entity + manual PK - save" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleVersionedEntityRepositoryManualPk.save(LifecycleVersionedEntityManualPk(Some(8), "john", "f1", None)))
    val read = executeAction(lifecycleVersionedEntityRepositoryManualPk.findOne(entity.id.get)).get
    read.field1 should equal("f1")
    entity.field1 should equal("postPersist")
  }

  it should "trigger a preUpdate event" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryManualPk.save(LifecycleEntityManualPk(Some(7), "john", "f1", "f2", "f3", "f4")))
    executeAction(lifecycleEntityRepositoryManualPk.update(entity.copy(name = "peter")))
    val read = executeAction(lifecycleEntityRepositoryManualPk.findOne(entity.id.get)).get
    read.field1 should equal("preUpdate")
  }

  it should "trigger a postUpdate event" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryManualPk.save(LifecycleEntityManualPk(Some(7), "john", "f1", "f2", "f3", "f4")))
    val updated = executeAction(lifecycleEntityRepositoryManualPk.update(entity.copy(name = "peter")))
    val read = executeAction(lifecycleEntityRepositoryManualPk.findOne(entity.id.get)).get
    read.field2 should equal("f2")
    updated.field2 should equal("postUpdate")
  }

  it should "trigger a preDelete event" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryManualPk.save(LifecycleEntityManualPk(Some(7), "john", "f1", "f2", "f3", "f4")))
    val deleted = executeAction(lifecycleEntityRepositoryManualPk.delete(entity))
    deleted.field3 should equal("preDelete")
  }

  it should "trigger a postDelete event" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryManualPk.save(LifecycleEntityManualPk(Some(7), "john", "f1", "f2", "f3", "f4")))
    val deleted = executeAction(lifecycleEntityRepositoryManualPk.delete(entity))
    deleted.field4 should equal("postDelete")
  }

  it should "executes multiple event handlers for the same event type" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryPrePersistAutoPkMultipleHandlers.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read = executeAction(lifecycleEntityRepositoryPrePersistAutoPkMultipleHandlers.findOne(entity.id.get)).get
    read.name should equal("prePersist")
    read.field2 should equal("prePersist")
  }

  it should "event handler as private method" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryPrivateHandler.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read = executeAction(lifecycleEntityRepositoryPrivateHandler.findOne(entity.id.get)).get
    read.name should equal("prePersist")
  }

  it should "event handler in trait" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryHandlersInTraits.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read = executeAction(lifecycleEntityRepositoryHandlersInTraits.findOne(entity.id.get)).get
    read.field1 should equal("prePersist")
    read.field2 should equal("postLoad")
  }

  it should "event handlers with same name as private methods are both called" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryPrivateHandlerSubClass.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read = executeAction(lifecycleEntityRepositoryPrivateHandlerSubClass.findOne(entity.id.get)).get

    // scala 2.10 does not support this
    if (!scala.util.Properties.versionString.startsWith("version 2.10")) {
      read.name should equal("prePersist")
      read.field1 should equal("prePersist")
    }
  }

  it should "event handlers with same name for different event types are both called" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryPrivateHandlerSubClassSameHandlerNameOtherHandlerType.save(LifecycleEntity(None, "john", "f1", "f2")))
    val read = executeAction(lifecycleEntityRepositoryPrivateHandlerSubClassSameHandlerNameOtherHandlerType.findOne(entity.id.get)).get

    // scala 2.10 does not support this
    if (!scala.util.Properties.versionString.startsWith("version 2.10")) {
      entity.name should equal("prePersist")
      read.field1 should equal("postLoad")
    }
  }

  it should "overrides an event handler" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val entity = executeAction(lifecycleEntityRepositoryPrePersistAutoPkMultipleHandlers.save(LifecycleEntity(None, "john", "f1", "f2")))
    entity.field1 should equal("postPersistOverride")
  }

  it should "successfully instantiate a repository with multiple handlers for different event types" in {
    new LifecycleEntityRepositoryMultipleHandlersDifferentType(driver)
  }

  it should "successfully instantiates a repository with multiple handlers for the same event type in different classes in the hierarchy" in {
    new LifecycleEntityRepositoryMultipleHandlersSameTypeSubClass(driver)
  }

  it should "fail to instantiate a repository with multiple handlers for the same event type in the same class" in {
    val exception =
      intercept[DuplicatedHandlerException] {
        new LifecycleEntityRepositoryMultipleHandlersSameType(driver)
      }
    exception.getMessage should equal("Only a single event handler for a given event type is allowed in the same repository class. Repository class: LifecycleEntityRepositoryMultipleHandlersSameType, eventType: postLoad")
  }

  it should "fail for a handler with an incorrect parameter type" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val exception =
      intercept[ListenerInvocationException] {
        executeAction(lifecycleEntityRepositoryHandlerWrongHandlerParameterType.save(LifecycleEntity(None, "john", "f1", "f2")))
      }
    exception.getMessage should equal("Error while invoking listener for event type prePersist in class com.byteslounge.slickrepo.test.LifecycleEntityRepositoryHandlerWrongHandlerParameterType. Confirm that the handler method accepts a single parameter which type is compatible with the repository entity type")
    exception.getCause.getClass should equal (classOf[IllegalArgumentException])
  }

  it should "fail for a handler with an incorrect number of parameters" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val exception =
      intercept[ListenerInvocationException] {
        executeAction(lifecycleEntityRepositoryHandlerWrongHandlerParameterNumber.save(LifecycleEntity(None, "john", "f1", "f2")))
      }
    exception.getMessage should equal("Error while invoking listener for event type prePersist in class com.byteslounge.slickrepo.test.LifecycleEntityRepositoryHandlerWrongHandlerParameterNumber. Confirm that the handler method accepts a single parameter which type is compatible with the repository entity type")
    if (scala.util.Properties.versionString.startsWith("version 2.10")) {
      exception.getCause.getClass should equal (classOf[IllegalArgumentException])
    } else {
      exception.getCause.getClass should equal (classOf[ArrayIndexOutOfBoundsException])
    }
  }
}

case class LifecycleEntity(override val id: Option[Int] = None, name: String, field1: String, field2: String) extends Entity[LifecycleEntity, Int] {
  def withId(id: Int): LifecycleEntity = this.copy(id = Some(id))
}

abstract class LifecycleEntityRepository(override val driver: JdbcProfile) extends Repository[LifecycleEntity, Int] {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[LifecycleEntities]
  type TableType = LifecycleEntities

  class LifecycleEntities(tag: slick.lifted.Tag) extends Table[LifecycleEntity](tag, "LIFECYC_ENT") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def field1 = column[String]("FIELD1")

    def field2 = column[String]("FIELD2")

    def * = (id.?, name, field1, field2) <> ((LifecycleEntity.apply _).tupled, LifecycleEntity.unapply)
  }

}

case class LifecycleEntityManualPk(override val id: Option[Int], name: String, field1: String, field2: String, field3: String, field4: String) extends Entity[LifecycleEntityManualPk, Int] {
  def withId(id: Int): LifecycleEntityManualPk = this.copy(id = Some(id))
}

class LifecycleEntityRepositoryManualPk(override val driver: JdbcProfile) extends Repository[LifecycleEntityManualPk, Int] {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[LifecycleEntitiesManualPk]
  type TableType = LifecycleEntitiesManualPk

  class LifecycleEntitiesManualPk(tag: slick.lifted.Tag) extends Table[LifecycleEntityManualPk](tag, "LIFECYC_ENT_MPK") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def field1 = column[String]("FIELD1")

    def field2 = column[String]("FIELD2")

    def field3 = column[String]("FIELD3")

    def field4 = column[String]("FIELD4")

    def * = (id.?, name, field1, field2, field3, field4) <> ((LifecycleEntityManualPk.apply _).tupled, LifecycleEntityManualPk.unapply)
  }

  @prePersist
  def prePersist(e: LifecycleEntityManualPk) = {
    e.copy(name = "prePersist")
  }

  @postPersist
  def postPersist(e: LifecycleEntityManualPk) = {
    e.copy(field1 = "postPersist")
  }

  @preUpdate
  def preUpdate(e: LifecycleEntityManualPk) = {
    e.copy(field1 = "preUpdate")
  }

  @postUpdate
  def postUpdate(e: LifecycleEntityManualPk) = {
    e.copy(field2 = "postUpdate")
  }

  @preDelete
  def preDelete(e: LifecycleEntityManualPk) = {
    e.copy(field3 = "preDelete")
  }

  @postDelete
  def postDelete(e: LifecycleEntityManualPk) = {
    e.copy(field4 = "postDelete")
  }
}

class LifecycleEntityRepositoryPostLoad(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @postLoad
  def postLoad(e: LifecycleEntity) = {
    e.copy(name = "postLoad")
  }
}

class LifecycleEntityRepositoryPrePersistAutoPk(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @prePersist
  def prePersist(e: LifecycleEntity) = {
    e.copy(name = "prePersist")
  }

  @postPersist
  def postPersist(e: LifecycleEntity) = {
    e.copy(field1 = "postPersist")
  }
}

class LifecycleEntityRepositoryPrePersistAutoPkMultipleHandlers(override val driver: JdbcProfile) extends LifecycleEntityRepositoryPrePersistAutoPk(driver) {
  @prePersist
  def otherPrePersist(e: LifecycleEntity) = {
    e.copy(field2 = "prePersist")
  }

  @postPersist
  override def postPersist(e: LifecycleEntity) = {
    e.copy(field1 = "postPersistOverride")
  }
}

class LifecycleEntityRepositoryPrivateHandler(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @prePersist
  private def prePersist(e: LifecycleEntity) = {
    e.copy(name = "prePersist")
  }
}

class LifecycleEntityRepositoryPrivateHandlerSubClass(override val driver: JdbcProfile) extends LifecycleEntityRepositoryPrivateHandler(driver) {
  @prePersist
  private def prePersist(e: LifecycleEntity) = {
    e.copy(field1 = "prePersist")
  }
}

class LifecycleEntityRepositoryPrivateHandlerSubClassSameHandlerNameOtherHandlerType(override val driver: JdbcProfile) extends LifecycleEntityRepositoryPrivateHandler(driver) {
  @postLoad
  private def prePersist(e: LifecycleEntity) = {
    e.copy(field1 = "postLoad")
  }
}

case class LifecycleVersionedEntity(override val id: Option[Int] = None, name: String, field1: String, override val version: Option[Int]) extends VersionedEntity[LifecycleVersionedEntity, Int, Int] {
  def withId(id: Int): LifecycleVersionedEntity = this.copy(id = Some(id))

  def withVersion(version: Int): LifecycleVersionedEntity = this.copy(version = Some(version))
}

abstract class LifecycleVersionedEntityRepository(override val driver: JdbcProfile) extends VersionedRepository[LifecycleVersionedEntity, Int, Int] {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[LifecycleVersionedEntities]
  type TableType = LifecycleVersionedEntities

  class LifecycleVersionedEntities(tag: slick.lifted.Tag) extends Table[LifecycleVersionedEntity](tag, "LIFECYC_V_ENT") with Versioned[Int, Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def field1 = column[String]("FIELD1")

    def version = column[Int]("VERSION")

    def * = (id.?, name, field1, version.?) <> ((LifecycleVersionedEntity.apply _).tupled, LifecycleVersionedEntity.unapply)
  }

}

case class LifecycleVersionedEntityManualPk(override val id: Option[Int] = None, name: String, field1: String, override val version: Option[Int]) extends VersionedEntity[LifecycleVersionedEntityManualPk, Int, Int] {
  def withId(id: Int): LifecycleVersionedEntityManualPk = this.copy(id = Some(id))

  def withVersion(version: Int): LifecycleVersionedEntityManualPk = this.copy(version = Some(version))
}

class LifecycleVersionedEntityRepositoryManualPk(override val driver: JdbcProfile) extends VersionedRepository[LifecycleVersionedEntityManualPk, Int, Int] {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[LifecycleVersionedEntitiesManualPk]
  type TableType = LifecycleVersionedEntitiesManualPk

  class LifecycleVersionedEntitiesManualPk(tag: slick.lifted.Tag) extends Table[LifecycleVersionedEntityManualPk](tag, "LIFECYC_V_ENT_MPK") with Versioned[Int, Int] {
    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def field1 = column[String]("FIELD1")

    def version = column[Int]("VERSION")

    def * = (id.?, name, field1, version.?) <> ((LifecycleVersionedEntityManualPk.apply _).tupled, LifecycleVersionedEntityManualPk.unapply)
  }

  @prePersist
  def prePersist(e: LifecycleVersionedEntityManualPk) = {
    e.copy(name = "prePersist")
  }

  @postPersist
  def postPersist(e: LifecycleVersionedEntityManualPk) = {
    e.copy(field1 = "postPersist")
  }
}

class LifecycleVersionedEntityRepositoryPostLoad(override val driver: JdbcProfile) extends LifecycleVersionedEntityRepository(driver) {
  @postLoad
  def postLoad(e: LifecycleVersionedEntity) = {
    e.copy(name = "postLoad")
  }
}

class LifecycleVersionedEntityRepositoryPrePersistAutoPk(override val driver: JdbcProfile) extends LifecycleVersionedEntityRepository(driver) {
  @prePersist
  def prePersist(e: LifecycleVersionedEntity) = {
    e.copy(name = "prePersist")
  }

  @postPersist
  def postPersist(e: LifecycleVersionedEntity) = {
    e.copy(field1 = "postPersist")
  }
}

class LifecycleEntityRepositoryMultipleHandlersDifferentType(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @postLoad
  def postLoad(e: LifecycleEntity) = {
    e.copy(name = "postLoad")
  }

  @prePersist
  def prePersist(e: LifecycleEntity) = {
    e.copy(name = "prePersist")
  }
}

class LifecycleEntityRepositoryMultipleHandlersSameType(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @postLoad
  def postLoad(e: LifecycleEntity) = {
    e.copy(name = "postLoad")
  }

  @postLoad
  def otherPostLoad(e: LifecycleEntity) = {
    e.copy(name = "postLoad")
  }
}

class LifecycleEntityRepositoryMultipleHandlersSameTypeSuperClass(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @postLoad
  def postLoad(e: LifecycleEntity) = {
    e.copy(name = "postLoad")
  }
}

class LifecycleEntityRepositoryMultipleHandlersSameTypeSubClass(override val driver: JdbcProfile) extends LifecycleEntityRepositoryMultipleHandlersSameTypeSuperClass(driver) {
  @postLoad
  def otherPostLoad(e: LifecycleEntity) = {
    e.copy(name = "postLoad")
  }
}

class LifecycleEntityRepositoryHandlerWrongHandlerParameterType(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @prePersist
  def prePersist(e: String) = {
    LifecycleEntity(None, "john", "f1", "f2")
  }
}

class LifecycleEntityRepositoryHandlerWrongHandlerParameterNumber(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) {
  @prePersist
  def prePersist(e: LifecycleEntity, e2: LifecycleEntity) = {
    e.copy(name = "postLoad")
  }
}

trait PrePersistTrait {
  @prePersist
  def prePersist(e: LifecycleEntity) = {
    e.copy(field1 = "prePersist")
  }
}

trait PostLoadTrait {
  @postLoad
  def postLoad(e: LifecycleEntity) = {
    e.copy(field2 = "postLoad")
  }
}

class LifecycleEntityRepositoryHandlersInTraits(override val driver: JdbcProfile) extends LifecycleEntityRepository(driver) with PrePersistTrait with PostLoadTrait {
}
