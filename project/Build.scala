import sbt._
import Keys._

object Build extends Build {

  val dependencies = Seq(
    "com.typesafe.slick" %% "slick" % "3.1.1",

    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
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

        testOptions in Test := Seq(Tests.Filter(baseFilter)),
        testOptions in AllDbsTest := Seq(Tests.Filter(allDbsFilter))
      )

  val dbPrefixes = Seq("MySQL")
  lazy val AllDbsTest = config("alldbs") extend Test

  def testName(name: String): String = name.substring(name.lastIndexOf('.') + 1)
  def allDbsFilter(name: String): Boolean = dbPrefixes.exists(p => testName(name) startsWith p)
  def baseFilter(name: String): Boolean = !allDbsFilter(name)
}
