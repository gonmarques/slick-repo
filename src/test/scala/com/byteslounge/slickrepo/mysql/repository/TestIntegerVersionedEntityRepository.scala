package com.byteslounge.slickrepo.mysql.repository

import com.byteslounge.slickrepo.mysql.domain.{TestIntegerVersionedEntities, TestIntegerVersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import slick.ast.BaseTypedType
import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._

class TestIntegerVersionedEntityRepository extends VersionedRepository[TestIntegerVersionedEntity, Int, TestIntegerVersionedEntities, Int](MySQLDriver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[TestIntegerVersionedEntities]
}
