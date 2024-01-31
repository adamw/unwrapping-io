import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.io",
  scalaVersion := "3.3.1"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.17" % Test

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false, name := "unwrapping-io")
  .aggregate(core)

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-stream" % "1.0.2",
      "dev.zio" %% "zio" % "2.0.21",
      "dev.zio" %% "zio-direct" % "1.0.0-RC7",
      "com.softwaremill.ox" %% "core" % "0.0.18",
      "io.getkyo" %% "kyo-direct" % "0.8.5",
      scalaTest
    )
  )
