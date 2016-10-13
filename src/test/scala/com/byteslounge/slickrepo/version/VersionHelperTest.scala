package com.byteslounge.slickrepo.version

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import com.byteslounge.slickrepo.h2.domain.TestIntegerVersionedEntity

class VersionHelperTest extends FlatSpec with Matchers {

  "The Version Helper" should "update an entity version with the integer initial value if the version is not set" in {
    val entity = new VersionHelper[TestIntegerVersionedEntity].process(TestIntegerVersionedEntity(None, 2, None))
    entity.version.get should equal(1)
  }

  it should "update an entity version with the next integer value if the version is set" in {
    val entity = new VersionHelper[TestIntegerVersionedEntity].process(TestIntegerVersionedEntity(None, 2, Some(1)))
    entity.version.get should equal(2)
  }

}