package com.byteslounge.slickrepo.h2.repository

import com.byteslounge.slickrepo.h2.domain.{Car, Cars}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

class CarRepository extends Repository[Car, Int, Cars](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Cars]
}
