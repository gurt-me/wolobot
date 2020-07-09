organization := "me.gurt"
name := "wolobot"

version := "0.1.1"

scalaVersion := "2.13.3"

scalacOptions ++= Seq(
  "-target:jvm-11",
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings",
)

val versions = new {
  val circe = "0.12.3"
}

libraryDependencies ++= Seq(
  "pircbot"                     % "pircbot"         % "1.5.0",
  "ch.qos.logback"              % "logback-classic" % "1.2.3",
  "com.iheart"                 %% "ficus"           % "1.4.7",
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.2",
  "org.typelevel"              %% "cats-core"       % "2.1.1",
  "org.typelevel"              %% "cats-effect"     % "2.1.3",
) ++ Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
).map(_ % versions.circe)

lazy val nyaaSi = RootProject(uri("https://github.com/gurt-me/NyaaSi-API.git"))
dependsOn(nyaaSi)

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class") => MergeStrategy.concat
  case path                          => MergeStrategy.defaultMergeStrategy(path)
}
