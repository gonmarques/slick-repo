package com.byteslounge.slickrepo.repository

import slick.driver.H2Driver;
import slick.driver.H2Driver.api._;
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.domain.Car
import com.byteslounge.slickrepo.domain.Cars

class CarRepository extends Repository[Car, Int, Cars](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Cars]
}
