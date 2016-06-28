package com.byteslounge.slickrepo.repository

import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfter
import slick.jdbc.JdbcBackend.Database;
import org.scalatest.Matchers
import slick.dbio.DBIOAction
import scala.concurrent.Await
import slick.dbio.NoStream
import scala.concurrent.duration.Duration
import com.byteslounge.slickrepo.domain.Person
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery
import com.byteslounge.slickrepo.domain.Persons

class RepositoryTest extends FlatSpec with BeforeAndAfter with Matchers {

  /*"The Repository" should "create an entity" in {
    val person = Person(Option.empty, "john");
    val repository = new PersonRepository;
    val id: Int = executeAction(repository.save(person))
    id should equal (9)
  }*/

  def executeAction[X](action: DBIOAction[X, NoStream, _]): X = {
    Await.result(db.run(action), Duration.Inf)
  }

  var db: Database = null

  before {
    initializeDb
    createSchema
  }

  after {
    dropSchema
    shutdownDb
  }

  def initializeDb() {
    db = Database.forConfig("test")
  }
  
  def shutdownDb() {
    db.close
  }

  def createSchema() {
    executeAction(DBIO.seq(TableQuery[Persons].schema.create))
  }

  def dropSchema() {
    executeAction(DBIO.seq(TableQuery[Persons].schema.drop))
  }
  
}