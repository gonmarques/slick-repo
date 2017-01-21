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

package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.repository._
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class AbstractRepositoryTest(val config: Config) extends FlatSpec with BeforeAndAfter with Matchers {

  val logger = LoggerFactory.getLogger(classOf[AbstractRepositoryTest])

  val driver = config.driver
  import driver.api._

  var db: Database = _

  val personRepository = new PersonRepository(driver)
  val carRepository = new CarRepository(driver)
  val coffeeRepository = new CoffeeRepository(driver)
  val testIntegerVersionedEntityRepository = new TestIntegerVersionedEntityRepository(driver)
  val testIntegerVersionedAutoPkEntityRepository = new TestIntegerVersionedAutoPkEntityRepository(driver)
  val testLongVersionedEntityRepository = new TestLongVersionedEntityRepository(driver)
  val testInstantVersionedEntityRepository = new TestInstantVersionedEntityRepository(driver)
  val testJodaTimeVersionedEntityRepository = new TestJodaTimeVersionedEntityRepository(driver)

  def executeAction[X](action: DBIOAction[X, NoStream, _]): X = {
    Await.result(db.run(action), Duration.Inf)
  }

  before {
    initializeDb()
    waitInitialized()
    createSchema()
    prepareTest()
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
    executeAction(
      DBIO.seq(
        personRepository.tableQuery.schema.create,
        carRepository.tableQuery.schema.create,
        coffeeRepository.tableQuery.schema.create,
        testIntegerVersionedEntityRepository.tableQuery.schema.create,
        testIntegerVersionedAutoPkEntityRepository.tableQuery.schema.create,
        testLongVersionedEntityRepository.tableQuery.schema.create,
        testInstantVersionedEntityRepository.tableQuery.schema.create,
        testJodaTimeVersionedEntityRepository.tableQuery.schema.create
      )
    )
  }

  def dropSchema() {
    executeAction(
      DBIO.seq(
        coffeeRepository.tableQuery.schema.drop,
        carRepository.tableQuery.schema.drop,
        personRepository.tableQuery.schema.drop,
        testIntegerVersionedEntityRepository.tableQuery.schema.drop,
        testIntegerVersionedAutoPkEntityRepository.tableQuery.schema.drop,
        testLongVersionedEntityRepository.tableQuery.schema.drop,
        testInstantVersionedEntityRepository.tableQuery.schema.drop,
        testJodaTimeVersionedEntityRepository.tableQuery.schema.drop
      )
    )
  }

  def waitInitialized() {
    val attempts = 20
    val sleep = 30000L
    var initialized = false
    (1 to attempts).foreach(
      i =>
        try {
          if(!initialized){
            val query = config.validationQuery
            executeAction(sql"#$query".as[(Int)].headOption)
            logger.info("Connected to database " + config.dbConfig)
            initialized = true
          }
        } catch {
          case e: Exception =>
            val message = if(i < attempts) "Will wait " + sleep + " ms before retry" else "Giving up"
            logger.warn("Could not connect to database " + config.dbConfig + " [attempt " + i + "]. " + message)
            if(i < attempts){
              Thread.sleep(sleep)
            }
        }
    )
  }

  def prepareTest() {
  }
}
