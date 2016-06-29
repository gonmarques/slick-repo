package com.byteslounge.slickrepo.repository

import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfter
import slick.jdbc.JdbcBackend.Database
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
import com.byteslounge.slickrepo.domain.Coffees
import com.byteslounge.slickrepo.domain.Coffee

class RepositoryTest extends FlatSpec with BeforeAndAfter with Matchers {

  val personRepository = new PersonRepository
  val carRepository = new CarRepository
  val coffeeRepository = new CoffeeRepository

  "The Repository" should "save an entity" in {
    val person = Person(None, "john")
    val id: Int = executeAction(personRepository.save(person))
    id should be > 0
  }

  it should "save related entities" in {
    val person = Person(None, "john")
    val personId: Int = executeAction(personRepository.save(person))
    val car = Car(None, "Benz", personId)
    val carId: Int = executeAction(carRepository.save(car))
    carId should be > 0
  }

  it should "read an entity" in {
    val person = Person(None, "john")
    val id: Int = executeAction(personRepository.save(person))
    val read: Person = executeAction(personRepository.findOne(id))
    id should equal(read.id.get)
  }

  it should "save an entity with a predefined ID" in {
    val coffee = Coffee(Option(78), "Some Coffee")
    val rowCount: Int = executeAction(coffeeRepository.saveWithId(coffee))
    rowCount should equal(1)
    val read: Coffee = executeAction(coffeeRepository.findOne(78))
    coffee.id.get should equal(read.id.get)
  }

  it should "search for an existing entity" in {
    val person = Person(None, "john")
    val id: Int = executeAction(personRepository.save(person))
    val read: Option[Person] = executeAction(personRepository.searchOne(id))
    id should equal(read.get.id.get)
  }

  it should "search for an entity that does not exist" in {
    val read: Option[Person] = executeAction(personRepository.searchOne(1))
    read should equal(None)
  }

  it should "delete an entity" in {
    val person = Person(None, "john")
    val id: Int = executeAction(personRepository.save(person))
    val read: Person = executeAction(personRepository.findOne(id))
    id should equal(read.id.get)
    val rowCount: Int = executeAction(personRepository.delete(id))
    rowCount should equal(1)
    val readAfterDelete: Option[Person] = executeAction(personRepository.searchOne(id))
    readAfterDelete should equal(None)
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
    executeAction(DBIO.seq(TableQuery[Persons].schema.create, TableQuery[Cars].schema.create, TableQuery[Coffees].schema.create))
  }

  def dropSchema() {
    executeAction(DBIO.seq(TableQuery[Coffees].schema.drop, TableQuery[Cars].schema.drop, TableQuery[Persons].schema.drop))
  }

}