package com.byteslounge.slickrepo.test

import com.byteslounge.slickrepo.repository._

abstract class RepositoryUpdateAutoPkTest(override val config: Config) extends RepositoryTest(config) {

  "The Repository (Auto PK entity)" should "update an entity with auto primary key" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val person: Person = executeAction(personRepository.save(Person(None, "john")))
    var updatedPerson: Person = person.copy(name = "smith")
    updatedPerson = executeAction(personRepository.update(updatedPerson))
    updatedPerson.id.get should equal(person.id.get)
    val read: Person = executeAction(personRepository.findOne(person.id.get))
    read.name should equal("smith")
  }
}