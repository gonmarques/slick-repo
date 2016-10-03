name := "slick-repo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.1.1",
  
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1" % "test",
  "com.h2database" % "h2" % "1.4.192" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.21" % "test"
)
