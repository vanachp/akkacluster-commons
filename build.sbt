name := "akkacluster-commons"

val SCALA_VERSION = "2.12.6"
val AKKA_VERSION = "2.5.12"

lazy val scalautils = project
  .in(file("."))
  .settings(basicSettings)
  .aggregate(clustering)
  .dependsOn(clustering)

lazy val clustering = project
  .in(file("clustering"))
  .settings(basicSettings)

lazy val basicSettings = Seq(
  scalaVersion := SCALA_VERSION,
  updateOptions := updateOptions.value.withCachedResolution(true),
  scalacOptions in Compile ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xlint",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Xfatal-warnings",
    "-Ywarn-dead-code"
  ),

  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor"               % AKKA_VERSION,
    "com.typesafe.akka" %% "akka-cluster"             % AKKA_VERSION,
    "com.typesafe.akka" %% "akka-remote"              % AKKA_VERSION
  )
)