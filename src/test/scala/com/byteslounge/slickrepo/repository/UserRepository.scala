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

import com.byteslounge.slickrepo.annotation.{prePersist, preUpdate}
import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.scalaversion.JdbcProfile
import slick.ast.BaseTypedType

trait Persistable[T] {
  def withCreatedTime(createdTime: Long): T
}

trait Updatable[T] extends Persistable[T]{
  def withUpdatedTime(updatedTime: Long): T
}

case class User(
                 id: Option[Int],
                 username: String,
                 createdTime: Option[Long],
                 updatedTime: Option[Long]
)
  extends Entity[User, Int]
  with Updatable[User] {

  def withId(id: Int): User = this.copy(id = Some(id))

  def withCreatedTime(createdTime: Long): User = this.copy(createdTime = Some(createdTime))

  def withUpdatedTime(updatedTime: Long): User = this.copy(updatedTime = Some(updatedTime))
}

abstract class PersistableRepository[T <: Persistable[T] with Entity[T, ID], ID](override val driver: JdbcProfile)
  extends Repository[T, ID] {

  @prePersist
  private def prePersist(entity: T): T = {
    entity.withCreatedTime(11)
  }
}

abstract class UpdatableRepository[T <: Updatable[T] with Entity[T, ID], ID](override val driver: JdbcProfile)
  extends PersistableRepository[T, ID](driver) {

  @preUpdate
  private def preUpdate(entity: T): T = {
    entity.withUpdatedTime(22)
  }
}

class UserRepository(override val driver: JdbcProfile) extends UpdatableRepository[User, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Users]
  type TableType = Users

  class Users(tag: slick.lifted.Tag) extends Table[User](tag, "USER") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    def createdTime = column[Long]("CREATED_TIME")
    def updatedTime = column[Option[Long]]("UPDATED_TIME")

    def * = (id.?, username, createdTime.?, updatedTime) <> ((User.apply _).tupled, User.unapply)
  }

  @prePersist
  @preUpdate
  private def normalizeUsername(user: User): User = {
    user.copy(username = user.username.toLowerCase())
  }
}
