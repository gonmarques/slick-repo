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
import com.byteslounge.slickrepo.domain.Car
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery
import com.byteslounge.slickrepo.domain.Persons
import com.byteslounge.slickrepo.domain.Cars

class RepositoryTest extends FlatSpec with BeforeAndAfter with Matchers {

  val personRepository = new PersonRepository;
  val carRepository = new CarRepository;

  "The Repository" should "create an entity" in {
    val person = Person(Option.empty, "john");
    val id: Int = executeAction(personRepository.save(person))
    id should be > 0
  }

  it should "create related entities" in {
    val person = Person(Option.empty, "john");
    val personId: Int = executeAction(personRepository.save(person))
    val car = Car(Option.empty, "Benz", personId);
    val carId: Int = executeAction(carRepository.save(car))
    carId should be > 0
  }

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
    executeAction(DBIO.seq(TableQuery[Persons].schema.create, TableQuery[Cars].schema.create))
  }

  def dropSchema() {
    executeAction(DBIO.seq(TableQuery[Cars].schema.drop, TableQuery[Persons].schema.drop))
  }

}