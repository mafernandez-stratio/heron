name := "heron"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.13",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.13" % Test,
  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.3" % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
  "org.json4s" %% "json4s-native" % "3.5.4",
  "org.json4s" %% "json4s-jackson" % "3.5.4"
)
