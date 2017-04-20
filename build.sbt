name := """lcbo-master"""
organization := "com.tim.peng"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

resolvers += Resolver.bintrayRepo("tabdulradi", "maven")
resolvers += "lightshed-maven" at "http://dl.bintray.com/content/lightshed/maven"

libraryDependencies ++= Seq(
  ws,
  filters,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "com.github.t3hnar" %% "scala-bcrypt" % "3.0",
  // use shaded JAR to avoid conflict with play ws' Netty, explaination: http://docs.datastax.com/en/developer/java-driver/3.2/manual/shaded_jar/
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.2.0" classifier "shaded" exclude("io.netty", "*"),
  "io.github.cassandra-scala" %% "troy" % "0.4.0",
  "ch.lightshed" %% "courier" % "0.1.4",
  "com.pauldijou" %% "jwt-core" % "0.12.1",
  "com.pauldijou" %% "jwt-play-json" % "0.12.1"
)

// add resources folder to Classpath to allow Tory find the schema.sql file
unmanagedClasspath in Compile += baseDirectory.value / "resources"