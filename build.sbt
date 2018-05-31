name              := "play-swagger" // 工程名称
organization      := "dripcom.swagger" // 组织
version           := "0.2-SNAPSHOT" // 工程版本号
scalaVersion      := "2.12.4"       // Scala 版本号
publishMavenStyle := true

lazy val root =
  project
    .in(file("."))
    .aggregate(
      `play-swagger-api`, // 宏的代码
      `play-swagger-core` // 测试用例的代码
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
    "org.scalacheck" %% "scalacheck"  % "1.13.4" % "test",
    "joda-time" % "joda-time" % "2.9.9"

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

// 设置 play-swagger-api 目录，并引入相关依赖
lazy val `play-swagger-api` = project
  .in(file("play-swagger-api"))
  .settings(commonSettings: _*)



// 设计 play-swagger-api 目录，并且引入相关 play 和 akka 的依赖
lazy val `play-swagger-core` = project
  .in(file("play-swagger-core"))
  .dependsOn(`play-swagger-api` % "compile->compile;test->test") //引入上面的 play-swagger-api 的包
.settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play"           % playVersion,
      "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
      "com.dripower" %% "play-circe" % "2609.1"
    ) ++ akkaDeps
)
