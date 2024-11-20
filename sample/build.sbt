lazy val akkaVersion = "2.10.0"
lazy val akkaHttpVersion = "10.7.0"
lazy val akkaManagementVersion = "1.6.0"
lazy val circeVersion = "0.14.8"
lazy val ironVersion = "2.6.0"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

// module は Java SE 9 以降で導入されたものであり、Java SE 8 準拠の場合は認識しないため破棄（discard）してしまって良い
// 破棄しなければ deduplicate エラーを起こす
// 参照： https://qiita.com/yokra9/items/1e72646623f962ce02ee
assembly / assemblyMergeStrategy := {
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" =>
    MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard // META-INF内のファイルを無視
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
assembly / assemblyJarName := "til-akka-typed.jar" // jar ファイル名を指定
assembly / mainClass := Some("com.kuramapommel.til_akka_typed.main") // mainClass を指定

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.kuramapommel.til_akka_typed",
      scalaVersion := "3.4.3",
      semanticdbEnabled := true
    )
  ),
  scalacOptions += {
    if (scalaVersion.value.startsWith("2.12"))
      "-Ywarn-unused-import"
    else
      "-Wunused:imports"
  },
  name := "sample",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-pki" % akkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
    "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
    "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "io.github.iltotore" %% "iron" % ironVersion,
    "io.github.iltotore" %% "iron-cats" % ironVersion,
    "com.fasterxml.uuid" % "java-uuid-generator" % "5.1.0",
    "commons-io" % "commons-io" % "2.17.0",
    "com.lightbend.akka" %% "akka-persistence-dynamodb" % "2.0.1",

    // test libraries
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.12" % Test
  ),
  dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.30"
)
