package com.byteslounge.slickrepo.repository

import slick.driver.H2Driver;
import slick.driver.H2Driver.api._;
import slick.ast.BaseTypedType
import com.byteslounge.slickrepo.domain.TestIntegerVersionedEntities
import com.byteslounge.slickrepo.domain.TestIntegerVersionedEntity

class TestIntegerVersionedEntityRepository extends VersionedRepository[TestIntegerVersionedEntity, Int, TestIntegerVersionedEntities, Int](H2Driver) {
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[TestIntegerVersionedEntities]
}
