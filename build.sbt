lazy val projectName = "scala-sftp"
lazy val projectOrganization = "com.earnest"

name := projectName
conflictManager := ConflictManager.latestRevision // this is the default, but we set it explicitly for IDEA. See: https://youtrack.jetbrains.com/issue/SCL-7646
organization := projectOrganization
cancelable in Global := true

scalacOptions ++= Seq (
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-deprecation",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard",
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:higherKinds")
scalacOptions in (Compile, console) := scalacOptions.value filterNot Set ("-Ywarn-unused-import", "-Ywarn-value-discard")

scalaVersion := "2.12.7"
updateOptions := updateOptions.value.withCachedResolution (true)
publishMavenStyle := false
dependencyOverrides += "org.typelevel" %% "cats-core" % "1.4.0" // latest cats-effect still relies on cats-core 1.3.1, but it is binary-compatible with 1.4.0
lazy val rootSettings = Seq.empty

lazy val libraries = Seq (
  "com.jcraft" % "jsch" % "0.1.54",
  "io.github.andrebeat" %% "scala-pool" % "0.4.1",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.typelevel" %% "cats-effect" % "1.0.0-1182d8c",
  "org.scalacheck" %% "scalacheck" % "1.13.4"
)

lazy val testLibraries = Seq (
  "org.scalatest" %% "scalatest" % "3.0.5"
)

libraryDependencies ++= libraries
libraryDependencies ++= testLibraries map (_ % Test)

addCompilerPlugin ("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
addCompilerPlugin ("org.spire-math" %% "kind-projector" % "0.9.3")
