package com.byteslounge.slickrepo.h2.repository

import com.byteslounge.slickrepo.h2.domain.{Car, Cars, Person, Persons}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

class PersonRepository extends Repository[Person, Int, Persons](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Persons]

  def findWithCarsOrderByIdAscAndCarIdDesc(): DBIO[Seq[(Person, Car)]] = {
    (tableQuery
      join TableQuery[Cars] on (_.id === _.idPerson))
      .map(x => (x._1, x._2))
      .sortBy(_._2.id.desc)
      .sortBy(_._1.id.asc)
      .result
  }

}
