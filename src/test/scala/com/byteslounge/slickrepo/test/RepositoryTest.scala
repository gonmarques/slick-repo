/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.byteslounge.slickrepo.test

import java.sql.SQLException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

import com.byteslounge.slickrepo.repository._
import slick.driver.H2Driver

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

  it should "find an entity" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val read: Person = executeAction(personRepository.findOne(person.id.get)).get
    person.id.get should equal(read.id.get)
  }

  it should "search for an entity that does not exist" in {
    val read: Option[Person] = executeAction(personRepository.findOne(1))
    read should equal(None)
  }

  it should "save an entity with a predefined ID" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val coffee: Coffee = executeAction(coffeeRepository.save(Coffee(Option(78), "Some Coffee")))
    val read: Coffee = executeAction(coffeeRepository.findOne(coffee.id.get)).get
    coffee.id.get should equal(read.id.get)
  }

  it should "delete an entity" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val read: Person = executeAction(personRepository.findOne(person.id.get)).get
    person.id.get should equal(read.id.get)
    val rowCount: Int = executeAction(personRepository.delete(person.id.get))
    rowCount should equal(1)
    val readAfterDelete: Option[Person] = executeAction(personRepository.findOne(person.id.get))
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
    val read: Coffee = executeAction(coffeeRepository.findOne(coffee.id.get)).get
    read.brand should equal("brand2")
  }

  it should "execute statements transactionally" in {

    import scala.concurrent.ExecutionContext.Implicits.global

    executeAction(coffeeRepository.save(Coffee(Option(1), "Some Coffee")))

    val work = for {
      readCoffee <- coffeeRepository.findOne(1)
      otherCoffee <- coffeeRepository.save(Coffee(Option(2), readCoffee.get.brand + "2"))
      person <- personRepository.save(Person(None, "john"))
    } yield (readCoffee, otherCoffee, person)

    val result: (Option[Coffee], Coffee, Person) = executeAction(coffeeRepository.executeTransactionally(work))

    result._1.get.id.get should equal(1)
    result._2.id.get should equal(2)
    result._3.id.get should be > 0

    val personCount: Int = executeAction(personRepository.count())
    personCount should equal(1)
    val readPerson: Person = executeAction(personRepository.findOne(result._3.id.get)).get
    readPerson.id.get should equal(result._3.id.get)

    val coffeeCount: Int = executeAction(coffeeRepository.count())
    val readCoffee1: Coffee = executeAction(coffeeRepository.findOne(1)).get
    val readCoffee2: Coffee = executeAction(coffeeRepository.findOne(2)).get
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
      case _: Exception                                                      => fail
    }

    val cofeeCount: Int = executeAction(coffeeRepository.count())
    cofeeCount should equal(1)
    val readCoffee: Coffee = executeAction(coffeeRepository.findOne(1)).get
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
    val coffee: Coffee = executeAction(coffeeRepository.findOne(maxPersonId + 1)).get
    coffee.id.get should equal(maxPersonId + 1)
  }

  it should "pessimistic lock entities" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val startLatch = new CountDownLatch(1)
    val endLatch = new CountDownLatch(2)
    val successCount = new AtomicInteger
    val failureCount = new AtomicInteger

    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    val car: Car = executeAction(carRepository.save(Car(None, "Benz", person.id.get)))

    def runnable(runnableId: Int) = new Runnable() {
      def run(): Unit = {
        startLatch.await()
        try{
          executeAction(
            personRepository.executeTransactionally(
              config.driver match {
                case _: H2Driver => lockTimeoutWorkH2(person)
                case _           => lockTimeoutWork(runnableId, person, car)
              }
            )
          )
          successCount.incrementAndGet()
        } catch {
          case sqle: SQLException if matchError(sqle, config.rowLockTimeoutError)  => failureCount.incrementAndGet()
          case _: Exception                                                        =>
        } finally {
          endLatch.countDown()
        }
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

  def lockTimeoutWork(runnableId: Int, person: Person, car: Car): DBIO[_] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      x <- if (runnableId == 1) personRepository.lock(person) else carRepository.lock(car)
      y <- DBIO.successful(Thread.sleep(2500))
      z <- if (runnableId == 1) carRepository.lock(car) else personRepository.lock(person)
    } yield (x, y, z)
  }

  def lockTimeoutWorkH2(person: Person): DBIO[_] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      x <- personRepository.lock(person)
      y <- DBIO.successful(Thread.sleep(1500))
    } yield (x, y)
  }

  private def matchError(exception: SQLException, error: Error): Boolean = {
    exception.getErrorCode == error.errorCode && exception.getSQLState == error.sqlState
  }

}