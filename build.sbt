organization := "me.gurt"
name := "wolobot"

version := "0.1"

scalaVersion := "2.13.2"

scalacOptions ++= Seq(
  "-target:jvm-11",
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings",
)

libraryDependencies ++= Seq(
  "pircbot"      % "pircbot"   % "1.5.0",
  "com.iheart"  %% "ficus"     % "1.4.7",
  "org.clapper" %% "classutil" % "1.5.1",
)

lazy val nyaaSi = RootProject(uri("https://github.com/gurt-me/NyaaSi-API.git"))
dependsOn(nyaaSi)

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class") => MergeStrategy.concat
  case path                          => MergeStrategy.defaultMergeStrategy(path)
}
