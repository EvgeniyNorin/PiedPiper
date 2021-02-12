
name := "PiedPiper"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  // json
  "io.circe" %% "circe-core" % "0.13.0",
  "io.circe" %% "circe-generic" % "0.13.0",
  "io.circe" %% "circe-parser" % "0.13.0",
  "de.heikoseeberger" %% "akka-http-circe" % "1.33.0",
  // database
  "org.tpolecat" %% "doobie-core" % "0.8.8",
  //config
  "com.github.pureconfig" %% "pureconfig" % "0.12.3",
  //server part
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  //logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  //enum
  "com.beachape" %% "enumeratum" % "1.6.0",
  "com.beachape" %% "enumeratum-circe" % "1.6.0",
  "com.beachape" %% "enumeratum-doobie" % "1.6.0",
  //pureconfig
  "com.github.pureconfig" %% "pureconfig" % "0.14.0",
  //email
  "com.github.daddykotex" %% "courier" % "3.0.0-M3a"
)

unmanagedBase := baseDirectory.value / "libs"
//unmanagedResourceDirectories in Runtime += { baseDirectory.value / "dist"}
scalacOptions ++= Seq(
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ypartial-unification"
)
assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
    MergeStrategy.singleOrError
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName in assembly := "app.jar"

mainClass in assembly := Some("com.piedpiper.AppStarter")