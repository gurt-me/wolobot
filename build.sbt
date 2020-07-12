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
  val circe  = "0.12.3"
  val nyaaSi = "e307c87877"
}

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "pircbot"                     % "pircbot"         % "1.5.0",
  "ch.qos.logback"              % "logback-classic" % "1.2.3",
  "com.github.gurt-me"          % "NyaaSi-API"      % versions.nyaaSi,
  "com.chuusai"                %% "shapeless"       % "2.3.3",
  "com.iheart"                 %% "ficus"           % "1.4.7",
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.2",
  "io.circe"                   %% "circe-core"      % versions.circe,
  "io.circe"                   %% "circe-generic"   % versions.circe,
  "io.circe"                   %% "circe-parser"    % versions.circe,
  "org.scalatest"              %% "scalatest"       % "3.2.0" % "test",
  "org.typelevel"              %% "cats-core"       % "2.1.1",
  "org.typelevel"              %% "cats-effect"     % "2.1.3",
)

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class") => MergeStrategy.concat
  case path                          => MergeStrategy.defaultMergeStrategy(path)
}
