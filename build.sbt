name := "play-swagger"
organization := "swagger"
version := "0.1.1-SNAPSHOT"
scalaVersion := "2.12.5"

lazy val root =
  project
    .in(file("."))
    .aggregate(
      `play-swagger-api`,
      `play-swagger-core`
    )

val commonSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation", "-Xlint"),
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.11.12", "2.12.4"),
  parallelExecution in Test := false,
  libraryDependencies ++= Seq(
    "com.chuusai"    %% "shapeless"   % "2.3.3",
    "org.typelevel"  %% "cats-effect" % "0.9",
    "org.scalatest"  %% "scalatest"   % "3.0.5" % "test",
    "org.scalacheck" %% "scalacheck"  % "1.13.4" % "test"
  )
)

val akkaVersion     = "2.5.11"
val akkaHttpVersion = "10.0.11"
val playVersion     = "2.6.11"

val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-stream",
  "com.typesafe.akka" %% "akka-slf4j"
).map(_ % akkaVersion)

lazy val `play-swagger-api` = project
  .in(file("play-swagger-api"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play"           % playVersion,
      "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
    ) ++ akkaDeps
  )

lazy val `play-swagger-core` = project
  .in(file("play-swagger-core"))
  .dependsOn(`play-swagger-api` % "compile->compile;test->test")
