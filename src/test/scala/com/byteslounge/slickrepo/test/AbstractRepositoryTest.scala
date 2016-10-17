package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.repository.{CarRepository, CoffeeRepository, PersonRepository, TestIntegerVersionedEntityRepository}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class AbstractRepositoryTest(val config: Config) extends FlatSpec with BeforeAndAfter with Matchers {

  val driver = config.driver
  import driver.api._

  var db: Database = _

  val personRepository = new PersonRepository(driver)
  val carRepository = new CarRepository(driver)
  val coffeeRepository = new CoffeeRepository(driver)
  val testIntegerVersionedEntityRepository = new TestIntegerVersionedEntityRepository(driver)

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
    db = Database.forConfig(config.dbConfig)
  }

  def shutdownDb() {
    db.close
  }

  def createSchema() {
    val max: Int = 10
    var attempts:Int = 1
    while (attempts <= max) {
      try {
        executeAction(DBIO.seq(personRepository.tableQuery.schema.create, carRepository.tableQuery.schema.create, coffeeRepository.tableQuery.schema.create, testIntegerVersionedEntityRepository.tableQuery.schema.create))
      } catch {
        case e: Exception =>
      }
      Thread.sleep(120000)
      attempts = attempts + 1
    }
  }

  def dropSchema() {
    executeAction(DBIO.seq(coffeeRepository.tableQuery.schema.drop, carRepository.tableQuery.schema.drop, personRepository.tableQuery.schema.drop, testIntegerVersionedEntityRepository.tableQuery.schema.drop))
  }

}
