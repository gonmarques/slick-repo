import sbt.Keys._
import sbt._

object Build extends Build {

  val dependencyResolvers = Seq("Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/")

  val dependencies = Seq(
    "com.typesafe.slick" %% "slick" % "3.1.1",

    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "com.typesafe.slick" %% "slick-extensions" % "3.1.0" % "test",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1" % "test",
    "com.h2database" % "h2" % "1.4.192" % "test",
    "mysql" % "mysql-connector-java" % "5.1.38" % "test",
    "org.slf4j" % "slf4j-simple" % "1.7.21" % "test"
  )

  lazy val project =
    Project("root", file("."))
      .configs(AllDbsTest)
      .settings(inConfig(AllDbsTest)(Defaults.testTasks): _*)
      .settings(
        name := "slick-repo",
        version := "1.0-SNAPSHOT",
        scalaVersion := "2.11.8",
        parallelExecution in Test := false,
        libraryDependencies ++= dependencies,
        resolvers ++= dependencyResolvers,

        testOptions in Test := Seq(Tests.Filter(baseFilter)),
        testOptions in AllDbsTest := Seq(Tests.Filter(allDbsFilter))
      )

  lazy val mysql =
    Project("mysql", file("src/docker/mysql"))
      .settings(
        name := "mysql"
      )

  lazy val oracle =
    Project("oracle", file("src/docker/oracle"))
      .settings(
        name := "oracle"
      )

  val dbPrefixes = Seq("MySQL", "Oracle")
  lazy val AllDbsTest = config("alldbs") extend Test

  def testName(name: String): String = name.substring(name.lastIndexOf('.') + 1)

  def allDbsFilter(name: String): Boolean = dbPrefixes.exists(p => testName(name) startsWith p)

  def baseFilter(name: String): Boolean = !allDbsFilter(name)
}
