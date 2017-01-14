# Slick CRUD Repositories

|Database|CI |Build status|
|--------|---|------------|
|MySQL, Oracle, DB2, PostgreSQL, Derby, H2, Hsql|Travis CI|[![Build status](https://travis-ci.org/gonmarques/slick-repo.svg?branch=master)](https://travis-ci.org/gonmarques/slick-repo)|
|SQLServer|AppVeyor|[![Build status](https://ci.appveyor.com/api/projects/status/3httes30fa1foes1/branch/master?svg=true)](https://ci.appveyor.com/project/gonmarques/slick-repo)|

[![Coverage Status](https://coveralls.io/repos/github/gonmarques/slick-repo/badge.svg?branch=master)](https://coveralls.io/github/gonmarques/slick-repo)&nbsp;&nbsp;&nbsp;[![Latest Release](https://img.shields.io/github/release/gonmarques/slick-repo.svg)](https://search.maven.org/#search%7Cga%7C1%7Cbyteslounge%20slick-repo)&nbsp;&nbsp;&nbsp;[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-7c39ef.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Slick Repositories is an aggregation of common database operations in ready-to-be-used generic and type-safe repositories, best known as DAOs.

## Main features

 - Provide common database operations like *save*, *update*, *find*, *delete* or *count* in a type-safe way
 - Other operations like Transactions, Optimistic Locking (aka versioning), Pessimistic Locking or custom query/statement execution are also supported
 - In order to maximize performance, all provided operations are backed by Slick compiled queries, as recommended in [Slick Documentation](http://slick.lightbend.com/doc/3.1.1/queries.html)

## Latest Release

The library releases are available at [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cbyteslounge%20slick-repo) for Scala **2.11** and **2.10**. In order to add the library as a dependency to your project:

```scala
libraryDependencies += "com.byteslounge" %% "slick-repo" % "1.2.2"
```

## Introduction

This library allows one to use generic repositories in order to execute common database operations for Slick entities. The following code snippet illustrates such a repository definition:

```scala
case class Coffee(override val id: Option[Int], brand: String) extends Entity[Coffee, Int]{
  def withId(id: Int): Coffee = this.copy(id = Some(id))
}

class CoffeeRepository(override val driver: JdbcProfile) extends Repository[Coffee, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Coffees]
  type TableType = Coffees

  class Coffees(tag: slick.lifted.Tag) extends Table[Coffee](tag, "COFFEE") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def brand = column[String]("BRAND")

    def * = (id.?, brand) <> ((Coffee.apply _).tupled, Coffee.unapply)
  }
}
```

It's a pretty usual Slick entity definition, with the subtle additional detail that we extend `Entity` in the case class. Note how we pass `Coffee` and `Int` as type parameters to the entity class. They represent the entity and the primary key type respectively.

The repository definition - `CoffeeRepository` - is also very straight forward: One extends `Repository` using the same type parameters as the entity (in this case `Coffee` and `Int`), and expect a `driver` to be passed in when a repository instance is created by the application (more on this later).

Since the Slick table definition (`Coffees`) needs to have a driver instance in scope, we must define it inside the repository. The table definition extends `Keyed` with the primary key type: `Int`.

This is pretty much everything one needs to define in order to have a type-safe generic repository. Now it's just a matter of using the repository:

```scala
import scala.concurrent.ExecutionContext.Implicits.global

val coffeeRepository = new CoffeeRepository(MySQLDriver)
val coffee: Future[Coffee] = db.run(coffeeRepository.save(Coffee(None, "Espresso")))
```

The repository instance - `coffeeRepository` - may be created only once and reused across the entire application.

The returned coffee instance will have a database auto-generated primary key assigned to its `id` field (we previously configured the entity primary key with Slick's `AutoInc`). **Note**: One may also use predefined primary keys if the Slick entity primary key is not configured as auto-increment. Everything is just plain Slick.

## Defining an implicit executor

It may be convenient to define an implicit database executor in order to avoid writing the `db.run(...)` expression everywhere:

```scala
object DatabaseExecutor {
  val db = Database.forConfig("mysql")
  implicit def executeOperation[T](databaseOperation: DBIO[T]): Future[T] = {
    db.run(databaseOperation)
  }
}
```

This implicit converter will automatically convert `DBIO` actions returned by the repository into Scala `Futures` by calling `db.run(...)`.

Now it's just a matter of bringing the implicit converter into scope where needed:

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import com.byteslounge.slickrepo.test.DatabaseExecutor._

val coffeeRepository = new CoffeeRepository(MySQLDriver)
val coffee: Future[Coffee] = coffeeRepository.save(Coffee(None, "Espresso"))
```

If necessary, the implicit converter may be imported only once per class (class level). Or it may be defined inside a trait and let classes that require the converter extend that trait.

Since there are so many different ways to define such an implicit executor, the library does not provide any out-of-the-box.

## Common database operations

The repositories support the following common database operations:

 - `def findAll(): DBIO[Seq[T]]`

 Find all entities

 - `def findOne(id: ID): DBIO[Option[T]]`

 Finds an entity by its primary key.

 - `def count(): DBIO[Int]`

 Counts all entities

 - `def save(entity: T): DBIO[T]`

 Saves an entity

 - `def update(entity: T): DBIO[T]`

 Updates an entity

 - `def delete(id: ID): DBIO[Int]`

 Deletes an entity

## Custom queries

It is possible to define custom queries or statements inside a repository. Even queries that access multiple tables:

```scala
class PersonRepository(override val driver: JdbcProfile) extends Repository[Person, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Persons]
  type TableType = Persons

  lazy val carRepository = new CarRepository(driver)

  class Persons(tag: slick.lifted.Tag) extends Table[Person](tag, "PERSON") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")

    def * = (id.?, name) <> ((Person.apply _).tupled, Person.unapply)
  }

  def findWithCarsOrderByIdAscAndCarIdDesc(): DBIO[Seq[(Person, Car)]] = {
    (tableQuery
      join carRepository.tableQuery on (_.id === _.idPerson))
      .map(x => (x._1, x._2))
      .sortBy(_._2.id.desc)
      .sortBy(_._1.id.asc)
      .result
  }
}
```

Note that every repository has a `tableQuery` field that holds the respective Slick `TableQuery`. Using this field may be useful to write Slick ad-hoc queries.

This example used a `CarRepository` instance declared inside `PersonRepository` in order to conveniently build a query that accesses both tables.

In order to keep the example simple, the custom query that was just implemented is not being pre-compiled by Slick. One should use pre-compiled queries as stated in [Slick Documentation](http://slick.lightbend.com/doc/3.1.1/queries.html). The operations that are provided out-of-the-box by the repositories are all pre-compiled Slick queries.

## Transactions

The repositories also provide a method - `executeTransactionally` - that wraps a unit of work that may be composed by multiple read/write operations within a single database transaction:

```scala
val work = for {
  person <- personRepository.save(Person(None, "John"))
  car <- carRepository.save(Car(None, "Benz", person.id.get))
} yield (person, car)

val result: Future[(Person, Car)] = db.run(personRepository.executeTransactionally(work))
```
