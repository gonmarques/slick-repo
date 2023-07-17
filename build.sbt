/*
 * MIT License
 *
 * Copyright (c) 2016 Gonçalo Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys._

val dependencyResolvers = Seq(
  "Typesafe Maven Repository" at "https://repo.typesafe.com/typesafe/maven-releases/"
)

val dependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.h2database" % "h2" % "1.4.192" % "test",
  "mysql" % "mysql-connector-java" % "5.1.38" % "test",
  "org.postgresql" % "postgresql" % "9.4.1211" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.21" % "test",
  "org.apache.derby" % "derby" % "10.11.1.1" % "test",
  "org.hsqldb" % "hsqldb" % "2.3.4" % "test",
  "joda-time" % "joda-time" % "2.9.6" % "test"
)

lazy val project: Project =
  Project("root", file("."))
    .configs(AllDbsTest, Db2Test, SqlServerTest)
    .settings(inConfig(AllDbsTest)(Defaults.testTasks): _*)
    .settings(inConfig(Db2Test)(Defaults.testTasks): _*)
    .settings(inConfig(SqlServerTest)(Defaults.testTasks): _*)
    .settings(
      name := "slick-repo",
      description := "CRUD Repositories for Slick based persistence Scala projects",
      version := "1.6.2-SNAPSHOT",
      scalaVersion := "2.12.16",
      crossScalaVersions := Seq("2.13.0", "2.12.16", "2.11.12", "2.10.7"),
      libraryDependencies ++= dependencies,
      libraryDependencies ++= scalaVersion(version =>
        Seq(
          getSlickDependency("slick", version),
          getSlickDependency("slick-hikaricp", version) % "test"
        ) ++
          (if (version.startsWith("2.10"))
             Seq("com.typesafe.slick" %% "slick-extensions" % "3.1.0" % "test")
           else Seq.empty) ++
          Seq("org.scala-lang" % "scala-reflect" % version)
      ).value,
      resolvers ++= dependencyResolvers,
      Test / parallelExecution := false,
      coverageEnabled := true,
      Test / testOptions := Seq(Tests.Filter(baseFilter)),
      Db2Test / testOptions := Seq(Tests.Filter(db2Filter)),
      AllDbsTest / testOptions := Seq(Tests.Filter(allDbsFilter)),
      SqlServerTest / testOptions := Seq(Tests.Filter(sqlServerFilter)),
      publishMavenStyle := true,
      organization := "com.byteslounge",
      pomIncludeRepository := { _ => false },
      Test / publishArtifact := false,
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
      useGpg := true,
      pomExtra :=
        <url>https://github.com/gonmarques/slick-repo</url>
          <inceptionYear>2016</inceptionYear>
          <licenses>
            <license>
              <name>MIT License</name>
              <url>https://opensource.org/licenses/MIT</url>
            </license>
          </licenses>
          <developers>
            <developer>
              <id>gonmarques</id>
              <name>Gonçalo Marques</name>
              <url>https://github.com/gonmarques</url>
            </developer>
          </developers>
          <contributors>
            <contributor>
              <name>Cláudio Diniz</name>
              <url>https://github.com/cdiniz</url>
            </contributor>
            <contributor>
              <name>Barnabás Oláh</name>
              <url>https://github.com/stsatlantis</url>
            </contributor>
            <contributor>
              <name>Marco Costa</name>
              <url>https://github.com/mabrcosta</url>
            </contributor>
            <contributor>
              <name>David Poetzsch-Heffter</name>
              <url>https://github.com/dpoetzsch</url>
            </contributor>
            <contributor>
              <name>George</name>
              <url>https://github.com/giannoug</url>
            </contributor>
          </contributors>
          <scm>
            <url>https://github.com/gonmarques/slick-repo.git</url>
            <connection>scm:git:git://github.com/gonmarques/slick-repo.git</connection>
          </scm>
    )

lazy val mysql: Project =
  Project("mysql", file("src/docker/mysql"))
    .settings(
      name := "mysql"
    )

lazy val oracleBuild: Project =
  Project("oracle-build", file("src/docker/oracle-build"))
    .settings(
      name := "oracle-build"
    )

lazy val db2: Project =
  Project("db2", file("src/docker/db2"))
    .settings(
      name := "db2"
    )

lazy val postgres: Project =
  Project("postgres", file("src/docker/postgres"))
    .settings(
      name := "postgres"
    )

val dbPrefixes = Seq("MySQL", "Oracle", "Postgres", "Derby", "Hsql")
val db2Prefix = Seq("DB2")
val sqlServerPrefix = Seq("SQLServer")
lazy val AllDbsTest: Configuration = config("alldbs") extend Test
lazy val Db2Test: Configuration = config("db2") extend Test
lazy val SqlServerTest: Configuration = config("sqlserver") extend Test

def testName(name: String): String = name.substring(name.lastIndexOf('.') + 1)

def allDbsFilter(name: String): Boolean =
  dbPrefixes.exists(p => testName(name) startsWith p)

def db2Filter(name: String): Boolean =
  db2Prefix.exists(p => testName(name) startsWith p)

def sqlServerFilter(name: String): Boolean =
  sqlServerPrefix.exists(p => testName(name) startsWith p)

def baseFilter(name: String): Boolean =
  !allDbsFilter(name) && !db2Filter(name) && !sqlServerFilter(name)

def getSlickDependency(slickComponent: String, version: String): ModuleID = {
  "com.typesafe.slick" %
    (slickComponent + "_" + version.substring(0, version.lastIndexOf('.'))) %
    (if (version.startsWith("2.10")) { "3.1.1" }
     else { "3.4.1" })
}
