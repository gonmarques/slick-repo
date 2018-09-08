|Database|CI |Build status|
|--------|---|------------|
|MySQL, Oracle, DB2, PostgreSQL, Derby, H2, Hsql|Travis CI|[![Build status](https://travis-ci.org/gonmarques/slick-repo.svg?branch=master)](https://travis-ci.org/gonmarques/slick-repo)|
|SQLServer|AppVeyor|[![Build status](https://ci.appveyor.com/api/projects/status/3httes30fa1foes1/branch/master?svg=true)](https://ci.appveyor.com/project/gonmarques/slick-repo)|

[![Coverage Status](https://coveralls.io/repos/github/gonmarques/slick-repo/badge.svg?branch=master)](https://coveralls.io/github/gonmarques/slick-repo)&nbsp;&nbsp;&nbsp;[![Latest Release](https://img.shields.io/badge/release-v1.5.1-007ec6.svg)](https://search.maven.org/#search%7Cga%7C1%7Cbyteslounge%20slick-repo)&nbsp;&nbsp;&nbsp;[![MIT License](https://img.shields.io/badge/license-MIT-7c39ef.svg)](http://opensource.org/licenses/MIT)

# slick-repo

Slick Repositories is an aggregation of common database operations in ready-to-be-used generic and type-safe repositories, best known as DAOs.

## Main features

 - Provide common database operations like *save*, *update*, *find*, *delete* or *count* in a type-safe way
 - Other operations like Transactions, Batch Insert, Optimistic Locking (aka versioning), Pessimistic Locking or custom query/statement execution are also supported
 - In order to maximize performance, all provided operations are backed by Slick compiled queries, as recommended in [Slick Documentation](http://slick.lightbend.com/doc/3.1.1/queries.html)

## Latest Release

The library releases are available at [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cbyteslounge%20slick-repo) for Scala **2.10**, **2.11** and **2.12**. In order to add the library as a dependency to your project:

```scala
libraryDependencies += "com.byteslounge" %% "slick-repo" % "1.5.1"
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

**Note**: By inheriting the `Entity` trait, the entity case class will have to implement method `withId`. As shown in the previous example, the method implementation should return a copy of the current entity instance with the `id` value that is passed as an argument to the method assigned to the entity `id` field.

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

 - `def delete(entity: T): DBIO[T]`

 Deletes an entity

 - `def batchInsert(entities: Seq[T]): DBIO[Option[Int]]`

 Performs a batch insert of an arbitrary sequence of entities

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

## Optimistic locking (versioning)

The repositories also allow the configuration of entities' optimistic locking. If an entity is configured as such, there will be a version field that will be automatically updated every time the entity is updated (and also when the entity is created by the very first time).

With every update, the entity version value is also checked if it's still the previous - and expected - value. If not, then another process managed to concurrently update the entity in the meantime which will result in an `OptimisticLockException` being thrown (in order to avoid an update miss).

### Versioning configuration

An entity and its respective repository may be configured for optimistic locking (versioning) like the following example:

```scala
case class Coffee(override val id: Option[Int], brand: String, override val version: Option[Int])
    extends VersionedEntity[Coffee, Int, Int]{
  def withId(id: Int): Coffee = this.copy(id = Some(id))
  def withVersion(version: Int): Coffee = this.copy(version = Some(version))
}

class CoffeeRepository(override val driver: JdbcProfile)
    extends VersionedRepository[Coffee, Int, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val versionType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Coffees]
  type TableType = Coffees

  class Coffees(tag: slick.lifted.Tag) extends Table[Coffee](tag, "COFFEE")
      with Versioned[Int, Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def brand = column[String]("BRAND")
    def version = column[Int]("VERSION")

    def * = (id.?, brand, version.?) <> ((Coffee.apply _).tupled, Coffee.unapply)
  }
}
```

Note that now the entity case class extends the `VersionedEntity` trait. This trait has three type parameters, where the first is the entity type (`Coffee`), the second is the primary key type (`Int`) and the third one is the version field type (`Int`).

The application should **never** try to manually set this field. The library is the sole responsible for automatically managing this field value. When creating a new instance of a versioned entity one should set the version field as `None`:

```scala
val coffee: Coffee = coffeeRepository.save(Coffee(None, "Ristretto", None))
```

The library will update this field automatically by calling the `withVersion` case class method that the application must define, as it was shown in the previous versioned entity configuration example. This method's implementation should return a copy of the current entity with its `version` field assigned with the version value that is passed as an argument of the method.

The application must also configure the repository to extend `VersionedRepository` by providing an additional type parameter that represents the version field type, and define the `versionType` field accordingly. Finally it is also required that the Slick table definition extends `Versioned` and provide the version type as a parameter and define the `version` column.

### Supported version types

The following version types are supported out-of-the-box:

 - `Int` - The version field will be an incrementing `Integer` value

 - `Long` - The version field will be an incrementing `Long` value

 - `com.byteslounge.slickrepo.version.InstantVersion` - The library will set the version field as a `java.sql.Timestamp` parameter in the underlying prepared statement. The timestamp will be built using the current UTC instant represented in the number of milliseconds since 1 January 1970.

 - `com.byteslounge.slickrepo.version.LongInstantVersion` - The library will set the version field as a `Long` value equal to the current UTC instant represented by the number of milliseconds since 1 January 1970.

 - `com.byteslounge.slickrepo.version.LocalDateTimeVersion` - The library will set the version field as a `java.sql.Timestamp` parameter in the underlying prepared statement. The timestamp will be built using the current local date and time, exactly like if it was obtained with `new java.util.Date()`. This means that the timestamp will be created using the number of milliseconds since 1 January 1970 until the local date and time as if it was in GMT. **Example**: The local date time is `2017-04-03 21:05:23.434`. The number of milliseconds that will be used to create the timestamp is `1491249923434`.

 - `com.byteslounge.slickrepo.version.LongLocalDateTimeVersion` - The library will use the same strategy as if the version field was of type `com.byteslounge.slickrepo.version.LocalDateTimeVersion` but it will store the value as a `Long` number containing the milliseconds since 1 January 1970.

### Custom version types

The library supports the configuration of custom version types provided by the application. In order to accomplish this, the application is only required to define an implicit version generator and bring it into the scope on the repository.

Supposing a use case where the application needs to version a given entity with a version field of type `String`, and the version value should be a `UUID`:

```scala
object VersionImplicits {

  implicit val uuidVersionGenerator = new VersionGenerator[String]{

    def initialVersion(): String = {
      getUUID()
    }

    def nextVersion(currentVersion: String): String = {
      getUUID()
    }

    private def getUUID(): String = {
      UUID.randomUUID().toString()
    }
  }
}
```

**Note**: In this case the `nextVersion` method is generating a new version value without taking the current version - `currentVersion` argument - into account. This is because we don't need to use a current UUID in order to generate the next UUID. The same does **not** apply to a version field that consists in an incrementing integer, for instance: in this case the current version value is required to generate the next version value: it should be the current value incremented by one.

Now it should be just a matter of bringing the implicit `uuidVersionGenerator` that was just created into the repository scope:

```scala
import com.byteslounge.slickrepo.version.VersionImplicits.uuidVersionGenerator

case class StringVersionedEntity(
  override val id: Option[Int],
  price: Double,
  override val version: Option[String]
) extends VersionedEntity[StringVersionedEntity, Int, String] {
  // ...
}

class StringVersionedEntityRepository(override val driver: JdbcProfile)
    extends VersionedRepository[StringVersionedEntity, Int, String](driver) {
  // ...
}
```

A good candidate for such a custom version type would be the requirement of storing the version field using a [Joda-Time](http://www.joda.org/joda-time/) timestamp representation.

## Pessimistic locking

The repositories provide a method for entity pessimistic locking:

 - `def lock(entity: T): DBIO[T]`

When such a method is called for a given entity, that entity will be pessimistically - or exclusively - locked for the duration of the current transaction (the transaction where the entity was locked). The lock will be released upon transaction commit or rollback.

## Entity lifecycle listeners

The repositories may define listeners that are invoked by the library when certain actions take place. For instance, a given repository may define a `prePersist` listener which will be invoked just before an entity that is managed by that repository is persisted:

```scala
import com.byteslounge.slickrepo.annotation.prePersist

class CoffeeRepository(override val driver: JdbcProfile) extends Repository[Coffee, Int](driver) {

  // ....

  @prePersist
  def setUsername(e: Coffee): Coffee = e.copy(username = currentUser())
}
```

In this example the repository is defining a `prePersist` listener that is responsible for setting up the current logged in user in a `Coffee` entity instance that is about to be persisted.

The following listeners are supported by the repositories:

 - `@postLoad`: Executed after an entity has been loaded.
 - `@prePersist`: Executed before an entity is persisted.
 - `@postPersist`: Executed after an entity has been persisted.
 - `@preUpdate`: Executed before an entity is updated.
 - `@postUpdate`: Executed after an entity has been updated.
 - `@preDelete`: Executed before an entity is deleted.
 - `@postDelete`: Executed after an entity has been deleted.

**Note**: Just like the `setUsername(e: Coffee): Coffee` example method that we have just seen above, all listener methods are expected to accept a single parameter which type must be compatible with the entity type that is handled by the repository. Each method return type must also be compatible with the repository entity type.

### Extended listener configuration

We may also create more complex scenarios with multiple listeners for the same event type, reuse listener methods, or use the same method in order to handle different event types.

Following next is an example definition that configures a `User` entity and repository that implement the following requirements:

 - When a user is saved for the first time its property `createdTime` is automatically set with the current timestamp
 - When a user is updated its property `updatedTime` is automatically set with the current timestamp
 - When a user is saved for the first time or updated, its property `username` is converted to lower case

```scala
trait Persistable[T] {
  def withCreatedTime(createdTime: Long): T
}

trait Updatable[T] extends Persistable[T]{
  def withUpdatedTime(updatedTime: Long): T
}

case class User(
                 id: Option[Int],
                 username: String,
                 createdTime: Option[Long],
                 updatedTime: Option[Long]
)
  extends Entity[User, Int]
  with Updatable[User] {

  def withId(id: Int): User = this.copy(id = Some(id))

  def withCreatedTime(createdTime: Long): User = this.copy(createdTime = Some(createdTime))

  def withUpdatedTime(updatedTime: Long): User = this.copy(updatedTime = Some(updatedTime))
}

abstract class PersistableRepository[T <: Persistable[T] with Entity[T, ID], ID](override val driver: JdbcProfile)
  extends Repository[T, ID](driver) {

  @prePersist
  private def prePersist(entity: T): T = {
    entity.withCreatedTime(Instant.now().toEpochMilli)
  }
}

abstract class UpdatableRepository[T <: Updatable[T] with Entity[T, ID], ID](override val driver: JdbcProfile)
  extends PersistableRepository[T, ID](driver) {

  @preUpdate
  private def preUpdate(entity: T): T = {
    entity.withUpdatedTime(Instant.now().toEpochMilli)
  }
}

class UserRepository(override val driver: JdbcProfile) extends UpdatableRepository[User, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Users]
  type TableType = Users

  class Users(tag: slick.lifted.Tag) extends Table[User](tag, "USER") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    def createdTime = column[Long]("CREATED_TIME")
    def updatedTime = column[Option[Long]]("UPDATED_TIME")

    def * = (id.?, username, createdTime.?, updatedTime) <> ((User.apply _).tupled, User.unapply)
  }

  @prePersist
  @preUpdate
  private def normalizeUsername(user: User): User = {
    user.copy(username = user.username.toLowerCase())
  }
}
```

**Note**: It is possible to define more than a single handler for the same event type by using parent or sub classes. In such case the handler execution order for that particular event type is parent class first.

## Usage examples

### Play Framework

 - [play-slick-rest](https://github.com/cdiniz/play-slick-rest)

### Akka HTTP

 - [slick-akka-http](https://github.com/cdiniz/slick-akka-http)
