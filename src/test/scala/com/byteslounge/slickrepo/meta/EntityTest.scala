package com.byteslounge.slickrepo.meta

import org.scalatest.FlatSpec
import com.byteslounge.slickrepo.domain.Person
import com.byteslounge.slickrepo.domain.Car
import org.scalatest.Matchers

class EntityTest extends FlatSpec with Matchers {

  "The Entity" should "not equal a null entity" in {
    val entity = Person(Some(1), "name")
    entity should not equal (null)
  }

  it should "not equal another entity if id is same and class is different" in {
    val entity = Person(Some(1), "name")
    val other = Car(Some(1), "other")
    entity should not equal (other)
  }

  it should "not equal another entity if id is different and class is same" in {
    val entity = Person(Some(1), "name")
    val other = Person(Some(2), "other")
    entity should not equal (other)
  }

  it should "not equal another entity if other id is empty" in {
    val entity = Person(Some(1), "name")
    val other = Person(None, "other")
    entity should not equal (other)
  }

  it should "not equal another entity if this id is empty" in {
    val entity = Person(None, "name")
    val other = Person(Some(1), "other")
    entity should not equal (other)
  }

  it should "not equal another entity if both ids are empty" in {
    val entity = Person(None, "name")
    val other = Person(None, "other")
    entity should not equal (other)
  }

  it should "equal another entity if id is same and class is same" in {
    val entity = Person(Some(1), "name")
    val other = Person(Some(1), "other")
    entity should equal(other)
  }

  it should "have hashcode equal to 1 if id is empty" in {
    val entity = Person(None, "name")
    entity.hashCode() should equal(1)
  }

  it should "have hashcode equal to the id hashcode if id is defined" in {
    val entity = Person(Some(3), "name")
    entity.hashCode() should equal(3)
  }

}