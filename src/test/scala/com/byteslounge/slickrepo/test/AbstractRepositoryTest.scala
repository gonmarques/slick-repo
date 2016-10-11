package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.domain._
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import slick.dbio.{DBIOAction, NoStream}
import slick.driver.H2Driver.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AbstractRepositoryTest extends FlatSpec with BeforeAndAfter with Matchers {

  var db: Database = _

  def executeAction[X](action: DBIOAction[X, NoStream, _]): X = {
    Await.result(db.run(action), Duration.Inf)
  }

  before {
    initializeDb()
    createSchema()
  }

  after {
    dropSchema()
    shutdownDb()
  }

  def initializeDb() {
    db = Database.forConfig("test")
  }

  def shutdownDb() {
    db.close
  }

  def createSchema() {
    executeAction(DBIO.seq(TableQuery[Persons].schema.create, TableQuery[Cars].schema.create, TableQuery[Coffees].schema.create, TableQuery[TestIntegerVersionedEntities].schema.create))
  }

  def dropSchema() {
    executeAction(DBIO.seq(TableQuery[Coffees].schema.drop, TableQuery[Cars].schema.drop, TableQuery[Persons].schema.drop, TableQuery[TestIntegerVersionedEntities].schema.drop))
  }

}
