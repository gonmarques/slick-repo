package com.byteslounge.slickrepo.meta

import org.scalatest.FlatSpec
import com.byteslounge.slickrepo.domain.Parent
import com.byteslounge.slickrepo.domain.Child

class EntityTest extends FlatSpec {

  "The Entity" should "not equal a null entity" in {
    val entity = Parent(Some(1), "name")
    assert(entity != null)
  }

  "The Entity" should "not equal another entity if id is same and class is different" in {
    val entity = Parent(Some(1), "name")
    val other = Child(Some(1), "other")
    assert(entity != other)
  }

  "The Entity" should "not equal another entity if id is different and class is same" in {
    val entity = Parent(Some(1), "name")
    val other = Parent(Some(2), "other")
    assert(entity != other)
  }

  "The Entity" should "not equal another entity other id is empty" in {
    val entity = Parent(Some(1), "name")
    val other = Parent(None, "other")
    assert(entity != other)
  }

  "The Entity" should "not equal another entity this id is empty" in {
    val entity = Parent(None, "name")
    val other = Parent(Some(1), "other")
    assert(entity != other)
  }

  "The Entity" should "not equal another entity both ids are empty" in {
    val entity = Parent(None, "name")
    val other = Parent(None, "other")
    assert(entity != other)
  }

  "The Entity" should "equal another entity if id is same and class is same" in {
    val entity = Parent(Some(1), "name")
    val other = Parent(Some(1), "other")
    assert(entity == other)
  }

  "The Entity" should "have hashcode equal to 1 if id is empty" in {
    val entity = Parent(None, "name")
    assert(entity.hashCode() == 1)
  }

  "The Entity" should "have hashcode equal to the id hashcode if id is defined" in {
    val entity = Parent(Some(3), "name")
    assert(entity.hashCode() == 3)
  }

}