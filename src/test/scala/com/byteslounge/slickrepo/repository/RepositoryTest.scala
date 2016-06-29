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

  it should "find all entities" in {
    val person1 = Person(None, "john")
    val id1: Int = executeAction(personRepository.save(person1))
    val person2 = Person(None, "john")
    val id2: Int = executeAction(personRepository.save(person2))
    val userIds: Seq[Int] = executeAction(personRepository.findAll())
      .map { p => p.id.get }
      .sortWith(_ < _)
    Seq(id1, id2) should equal(userIds)
  }

  it should "update an entity" in {
    val person = Person(None, "john")
    val id: Int = executeAction(personRepository.save(person))
    val keyedPerson = person.copy(id = Option(id))
    val updatedPerson = keyedPerson.copy(name = "smith")
    val rowCount: Int = executeAction(personRepository.update(updatedPerson))
    rowCount should equal(1)
    val read: Person = executeAction(personRepository.findOne(id))
    read.name should equal("smith")
  }

  it should "execute statements transactionally" in {

    import scala.concurrent.ExecutionContext.Implicits.global

    val coffee = Coffee(Option(1), "Some Coffee")
    executeAction(coffeeRepository.saveWithId(coffee))

    var work = (for {
      readCoffee <- coffeeRepository.findOne(1)
      rowCount <- coffeeRepository.saveWithId(Coffee(Option(2), readCoffee.brand + "2"))
      personId <- personRepository.save(Person(None, "john"))
    } yield (readCoffee, rowCount, personId))

    val result: (Coffee, Int, Int) = executeAction(coffeeRepository.executeTransactionally(work))
    
    result._1.id.get should equal(1)
    result._2 should equal(1)
    result._3 should be > 0

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