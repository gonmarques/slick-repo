package com.byteslounge.slickrepo.mysql.repository

import com.byteslounge.slickrepo.mysql.domain.{Car, Cars}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._

class CarRepository extends Repository[Car, Int, Cars](MySQLDriver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Cars]
}
