import play.PlayScala

name := "S3Signer"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
version := scala.util.Properties.envOrElse("BUILD_VERSION", "DEV")

scalaVersion := "2.11.4"
scalacOptions ++= Seq("-feature", "-target:jvm-1.8")
libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.9.9",
  "com.github.nscala-time" %% "nscala-time" % "1.6.0"
)

//----
// BuildInfo object configuration
//----
buildInfoSettings
sourceGenerators in Compile <+= buildInfo
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoKeys ++= Seq[BuildInfoKey](
  BuildInfoKey.action("gitCommit") {
    scala.util.Properties.envOrElse("GIT_COMMIT", "")
  },
  BuildInfoKey.action("buildTime") {
    if(version.value != "DEV") System.currentTimeMillis else ""
  }
)

buildInfoPackage := "com.rmeans.s3signer.appversion"
