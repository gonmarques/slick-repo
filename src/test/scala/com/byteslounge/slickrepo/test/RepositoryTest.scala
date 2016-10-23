package com.byteslounge.slickrepo.test

import java.sql.SQLException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

import com.byteslounge.slickrepo.repository._
import com.typesafe.slick.driver.db2.DB2Driver
import com.typesafe.slick.driver.ms.SQLServerDriver
import com.typesafe.slick.driver.oracle.OracleDriver
import slick.driver.{H2Driver, MySQLDriver, PostgresDriver}

abstract class RepositoryTest(override val config: Config) extends AbstractRepositoryTest(config) {

  import driver.api._

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
    val coffee: Coffee = executeAction(coffeeRepository.save(Coffee(Option(78), "Some Coffee")))
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

  it should "update an entity with manual primary key" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val coffee: Coffee = executeAction(coffeeRepository.save(Coffee(Option(1), "brand1")))
    var updatedCoffee: Coffee = coffee.copy(brand = "brand2")
    updatedCoffee = executeAction(coffeeRepository.update(updatedCoffee))
    updatedCoffee.id.get should equal(coffee.id.get)
    val read: Coffee = executeAction(coffeeRepository.findOne(coffee.id.get))
    read.brand should equal("brand2")
  }

  it should "execute statements transactionally" in {

    import scala.concurrent.ExecutionContext.Implicits.global

    executeAction(coffeeRepository.save(Coffee(Option(1), "Some Coffee")))

    val work = for {
      readCoffee <- coffeeRepository.findOne(1)
      otherCoffee <- coffeeRepository.save(Coffee(Option(2), readCoffee.brand + "2"))
      person <- personRepository.save(Person(None, "john"))
    } yield (readCoffee, otherCoffee, person)

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

    executeAction(coffeeRepository.save(Coffee(Option(1), "Some Coffee")))

    val work = for {
      _ <- personRepository.save(Person(None, "john"))
      _ <- coffeeRepository.save(Coffee(Option(2), "Some Coffee2"))
      _ <- coffeeRepository.save(Coffee(Option(2), "Duplicated ID"))
    } yield ()

    try {
      executeAction(coffeeRepository.executeTransactionally(work))
    } catch {
      case sqle: SQLException if matchError(sqle, config.rollbackTxError)    =>
      case e: Exception                                                      => fail
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

    val query = personRepository.tableQuery.map(_.id).max.result

    val work = for {
      maxId <- query
      _ <- coffeeRepository.save(Coffee(Option(maxId.get + 1), "Some Coffee"))
    } yield ()

    executeAction(coffeeRepository.executeTransactionally(work))

    val maxPersonId = math.max(person1.id.get, person2.id.get)
    val coffee: Coffee = executeAction(coffeeRepository.findOne(maxPersonId + 1))
    coffee.id.get should equal(maxPersonId + 1)
  }

  it should "pessimistic lock entities" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val startLatch = new CountDownLatch(1)
    val endLatch = new CountDownLatch(2)
    val successCount = new AtomicInteger
    val failureCount = new AtomicInteger

    val person1: Person = executeAction(personRepository.save(Person(None, "john")))
    val person2: Person = executeAction(personRepository.save(Person(None, "doe")))

    def runnable(runnableId: Int) = new Runnable() {
      def run() = {
        startLatch.await()
        try{
          executeAction(personRepository.executeTransactionally(lockTimeoutWork(runnableId, person1, person2)))
          successCount.incrementAndGet()
        } catch {
          case sqle: SQLException if matchError(sqle, config.rowLockTimeoutError)  => failureCount.incrementAndGet()
          case e: Exception                                                        =>
        }

        endLatch.countDown()
      }
    }

    successCount.get() should equal(0)
    failureCount.get() should equal(0)

    new Thread(runnable(1)).start()
    new Thread(runnable(2)).start()

    Thread.sleep(200)
    startLatch.countDown()
    endLatch.await()

    successCount.get() should equal(1)
    failureCount.get() should equal(1)
  }

  def lockTimeoutWork(runnableId: Int, person1: Person, person2: Person): DBIO[_] = {

    import scala.concurrent.ExecutionContext.Implicits.global

    config.driver match {
      case _: H2Driver =>
        for {
          x <- personRepository.lock(person1)
          y <- DBIO.successful(Thread.sleep(2500))
        } yield (x, y)
      case _: MySQLDriver | OracleDriver | DB2Driver | PostgresDriver | SQLServerDriver =>
        for {
          x <- personRepository.lock(if (runnableId == 1) person1 else person2)
          y <- DBIO.successful(Thread.sleep(2500))
          z <- personRepository.lock(if (runnableId == 1) person2 else person1)
        } yield (x, y, z)
    }
  }

  private def matchError(exception: SQLException, error: Error): Boolean = {
    exception.getErrorCode == error.errorCode && exception.getSQLState == error.sqlState
  }

}