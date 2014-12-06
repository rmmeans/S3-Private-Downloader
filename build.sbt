import play.PlayScala

name := "S3Downloader"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
version := scala.util.Properties.envOrElse("BUILD_VERSION", "DEV")

scalaVersion := "2.11.4"
scalacOptions ++= Seq("-feature", "-target:jvm-1.8")
libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.9.9",
  "com.github.nscala-time" %% "nscala-time" % "1.6.0"
)
