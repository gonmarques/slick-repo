package com.byteslounge.slickrepo.h2.repository

import com.byteslounge.slickrepo.h2.domain.{Coffee, Coffees}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

class CoffeeRepository extends Repository[Coffee, Int, Coffees](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Coffees]
}
