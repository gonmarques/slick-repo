package com.byteslounge.slickrepo.mysql.repository

import com.byteslounge.slickrepo.mysql.domain.{Coffee, Coffees}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._

class CoffeeRepository extends Repository[Coffee, Int, Coffees](MySQLDriver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Coffees]
}
