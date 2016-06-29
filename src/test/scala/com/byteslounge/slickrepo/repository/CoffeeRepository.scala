package com.byteslounge.slickrepo.repository

import slick.driver.H2Driver;
import slick.driver.H2Driver.api._;
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.domain.Coffee
import com.byteslounge.slickrepo.domain.Coffees

class CoffeeRepository extends Repository[Coffee, Int, Coffees](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Coffees]
}
