package com.byteslounge.slickrepo.mysql.repository

import com.byteslounge.slickrepo.mysql.domain.{Car, Cars, Person, Persons}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._

class PersonRepository extends Repository[Person, Int, Persons](MySQLDriver) {
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
