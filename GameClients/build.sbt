name := "GameClients"

version := "0.1"

scalaVersion := "2.12.8"


val akkaVersion = "2.5.20"
val akkaHttpVersion = "10.1.8"
val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0", // scala logger (base : slf4j) : https://github.com/typesafehub/scala-logging
  "de.heikoseeberger" %% "akka-http-circe" % "1.22.0",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.8",
  "ch.qos.logback" % "logback-classic" % "1.2.1",
  "ch.qos.logback" % "logback-core" % "1.2.1",
  "ch.qos.logback" % "logback-access" % "1.2.1",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "com.typesafe.akka" %% "akka-persistence"   % akkaVersion

)
libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.12" % akkaVersion
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic-extras"
).map(_ % circeVersion)


mainClass in assembly := Some("com.cagatay.gameclients.Main")

enablePlugins(JavaAppPackaging)

// universal in directory komutu icin
enablePlugins(UniversalPlugin)
