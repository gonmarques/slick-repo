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

package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.repository._

abstract class RepositoryUpdateAutoPkTest(override val config: Config) extends RepositoryTest(config) {

  "The Repository (Auto PK entity)" should "update an entity with auto primary key" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    var updatedPerson: Person = person.copy(name = "smith")
    updatedPerson = executeAction(personRepository.update(updatedPerson))
    updatedPerson.id.get should equal(person.id.get)
    val read: Person = executeAction(personRepository.findOne(person.id.get)).get
    read.name should equal("smith")
  }
}