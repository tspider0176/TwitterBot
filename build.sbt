name := """TwitterBot"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  ws,
  specs2 % Test,
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.typesafe.slick" % "slick_2.11" % "3.0.3",
  "com.typesafe.play" % "play-slick_2.11" % "1.0.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
