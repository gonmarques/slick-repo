package com.byteslounge.slickrepo.repository

import slick.driver.H2Driver;
import slick.driver.H2Driver.api._;
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.domain.Person
import com.byteslounge.slickrepo.domain.Persons

class PersonRepository extends Repository[Person, Int, Persons](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Persons]
}