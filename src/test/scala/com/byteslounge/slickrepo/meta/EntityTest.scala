/*
 * Copyright 2016 byteslounge.com (Gonçalo Marques).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.byteslounge.slickrepo.meta

import com.byteslounge.slickrepo.repository.{Car, Person}
import org.scalatest.{FlatSpec, Matchers}

class EntityTest extends FlatSpec with Matchers {

  "The Entity" should "not equal a null entity" in {
    val entity = Person(Some(1), "name")
    entity should not equal null
  }

  it should "not equal another entity if id is same and class is different" in {
    val entity = Person(Some(1), "name")
    val other = Car(Some(1), "other", 1)
    entity should not equal other
  }

  it should "not equal another entity if id is different and class is same" in {
    val entity = Person(Some(1), "name")
    val other = Person(Some(2), "other")
    entity should not equal other
  }

  it should "not equal another entity if other id is empty" in {
    val entity = Person(Some(1), "name")
    val other = Person(None, "other")
    entity should not equal other
  }

  it should "not equal another entity if this id is empty" in {
    val entity = Person(None, "name")
    val other = Person(Some(1), "other")
    entity should not equal other
  }

  it should "not equal another entity if both ids are empty" in {
    val entity = Person(None, "name")
    val other = Person(None, "other")
    entity should not equal other
  }

  it should "equal another entity if id is same and class is same" in {
    val entity = Person(Some(1), "name")
    val other = Person(Some(1), "other")
    entity should equal (other)
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