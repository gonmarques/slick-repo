package com.byteslounge.slickrepo.repository

import slick.driver.H2Driver;
import slick.driver.H2Driver.api._;
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.domain.Person
import com.byteslounge.slickrepo.domain.Persons
import com.byteslounge.slickrepo.domain.Cars
import com.byteslounge.slickrepo.domain.Car

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
