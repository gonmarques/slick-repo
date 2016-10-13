package com.byteslounge.slickrepo.h2.repository

import com.byteslounge.slickrepo.h2.domain.{TestIntegerVersionedEntities, TestIntegerVersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import slick.ast.BaseTypedType
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

class TestIntegerVersionedEntityRepository extends VersionedRepository[TestIntegerVersionedEntity, Int, TestIntegerVersionedEntities, Int](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[TestIntegerVersionedEntities]
}
