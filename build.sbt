
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
  "org.tpolecat" %% "doobie-core"      % "0.8.8",
  //config
  "com.github.pureconfig" %% "pureconfig" % "0.12.3",
  //server part
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  //logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  //enum
  "com.beachape" %% "enumeratum" % "1.6.1"
)

unmanagedBase := baseDirectory.value / "libs"

assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName in assembly := "app.jar"

mainClass in assembly := Some("com.piedpiper.AppStarter")