name := """play-scala"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "mysql" % "mysql-connector-java" % "5.1.12",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
)
