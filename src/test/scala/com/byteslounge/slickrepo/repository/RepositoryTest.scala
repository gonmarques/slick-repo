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
import java.sql.SQLException

class RepositoryTest extends FlatSpec with BeforeAndAfter with Matchers {

  val personRepository = new PersonRepository
  val carRepository = new CarRepository
  val coffeeRepository = new CoffeeRepository

  "The Repository" should "save an entity" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    person.id.get should be > 0
  }

  it should "save related entities" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val car: Car = executeAction(carRepository.save(Car(None, "Benz", person.id.get)))
    car.id.get should be > 0
  }

  it should "read an entity" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val read: Person = executeAction(personRepository.findOne(person.id.get))
    person.id.get should equal(read.id.get)
  }

  it should "save an entity with a predefined ID" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val coffee: Coffee = executeAction(coffeeRepository.saveWithId(Coffee(Option(78), "Some Coffee")))
    val read: Coffee = executeAction(coffeeRepository.findOne(coffee.id.get))
    coffee.id.get should equal(read.id.get)
  }

  it should "search for an existing entity" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val read: Option[Person] = executeAction(personRepository.searchOne(person.id.get))
    person.id.get should equal(read.get.id.get)
  }

  it should "search for an entity that does not exist" in {
    val read: Option[Person] = executeAction(personRepository.searchOne(1))
    read should equal(None)
  }

  it should "delete an entity" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val read: Person = executeAction(personRepository.findOne(person.id.get))
    person.id.get should equal(read.id.get)
    val rowCount: Int = executeAction(personRepository.delete(person.id.get))
    rowCount should equal(1)
    val readAfterDelete: Option[Person] = executeAction(personRepository.searchOne(person.id.get))
    readAfterDelete should equal(None)
  }

  it should "find all entities" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person1: Person = executeAction(personRepository.save(Person(None, "john")))
    val person2: Person = executeAction(personRepository.save(Person(None, "john")))
    val userIds: Seq[Int] = executeAction(personRepository.findAll())
      .map { p => p.id.get }
      .sortWith(_ < _)
    Seq(person1.id.get, person2.id.get) should equal(userIds)
  }

  it should "update an entity" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val updatedPerson = person.copy(name = "smith")
    val rowCount: Int = executeAction(personRepository.update(updatedPerson))
    rowCount should equal(1)
    val read: Person = executeAction(personRepository.findOne(person.id.get))
    read.name should equal("smith")
  }

  it should "execute statements transactionally" in {

    import scala.concurrent.ExecutionContext.Implicits.global

    executeAction(coffeeRepository.saveWithId(Coffee(Option(1), "Some Coffee")))

    var work = (for {
      readCoffee <- coffeeRepository.findOne(1)
      otherCoffee <- coffeeRepository.saveWithId(Coffee(Option(2), readCoffee.brand + "2"))
      person <- personRepository.save(Person(None, "john"))
    } yield (readCoffee, otherCoffee, person))

    val result: (Coffee, Coffee, Person) = executeAction(coffeeRepository.executeTransactionally(work))

    result._1.id.get should equal(1)
    result._2.id.get should equal(2)
    result._3.id.get should be > 0

    val personCount: Int = executeAction(personRepository.count())
    personCount should equal(1)
    val readPerson: Person = executeAction(personRepository.findOne(result._3.id.get))
    readPerson.id.get should equal(result._3.id.get)

    val coffeeCount: Int = executeAction(coffeeRepository.count())
    val readCoffee1: Coffee = executeAction(coffeeRepository.findOne(1))
    val readCoffee2: Coffee = executeAction(coffeeRepository.findOne(2))
    coffeeCount should equal(2)
    readCoffee1.id.get should equal(1)
    readCoffee1.brand should equal("Some Coffee")
    readCoffee2.id.get should equal(2)
    readCoffee2.brand should equal("Some Coffee2")
  }

  it should "rollback a transaction" in {

    import scala.concurrent.ExecutionContext.Implicits.global

    executeAction(coffeeRepository.saveWithId(Coffee(Option(1), "Some Coffee")))

    var work = (for {
      _ <- personRepository.save(Person(None, "john"))
      _ <- coffeeRepository.saveWithId(Coffee(Option(2), "Some Coffee2"))
      _ <- coffeeRepository.saveWithId(Coffee(Option(2), "Duplicated ID"))
    } yield ())

    try {
      executeAction(coffeeRepository.executeTransactionally(work))
    } catch {
      case sqle: SQLException if (sqle.getErrorCode == 23505) =>
      case e: Exception                                       => fail
    }

    val cofeeCount: Int = executeAction(coffeeRepository.count())
    cofeeCount should equal(1)
    val readCoffee: Coffee = executeAction(coffeeRepository.findOne(1))
    readCoffee.id.get should equal(1)
    val personCount: Int = executeAction(personRepository.count())
    personCount should equal(0)
  }

  it should "count entities" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    executeAction(personRepository.save(Person(None, "john")))
    executeAction(personRepository.save(Person(None, "smith")))
    val count: Int = executeAction(personRepository.count())
    count should equal(2)
  }

  it should "execute custom queries" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person1: Person = executeAction(personRepository.save(Person(None, "john")))
    val person2: Person = executeAction(personRepository.save(Person(None, "smith")))
    val car1: Car = executeAction(carRepository.save(Car(None, "Benz", person1.id.get)))
    val car2: Car = executeAction(carRepository.save(Car(None, "Chevrolet", person1.id.get)))
    val car3: Car = executeAction(carRepository.save(Car(None, "Toyota", person2.id.get)))
    val result: Seq[(Person, Car)] = executeAction(personRepository.findWithCarsOrderByIdAscAndCarIdDesc())
    result.size should equal(3)
    val resultIds: Seq[(Int, Int)] = result.map { case (p, c) => (p.id.get, c.id.get) }
    resultIds should equal(Seq((person1.id.get, car2.id.get), (person1.id.get, car1.id.get), (person2.id.get, car3.id.get)))
  }

  it should "execute ad-hoc statements within transactions" in {

    import scala.concurrent.ExecutionContext.Implicits.global

    val person1: Person = executeAction(personRepository.save(Person(None, "john")))
    val person2: Person = executeAction(personRepository.save(Person(None, "smith")))

    val query = TableQuery[Persons].map(_.id).max.result

    var work = (for {
      maxId <- query
      _ <- coffeeRepository.saveWithId(Coffee(Option(maxId.get + 1), "Some Coffee"))
    } yield ())

    executeAction(coffeeRepository.executeTransactionally(work))

    val maxPersonId = math.max(person1.id.get, person2.id.get)
    val coffee: Coffee = executeAction(coffeeRepository.findOne(maxPersonId + 1))
    coffee.id.get should equal(maxPersonId + 1)
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