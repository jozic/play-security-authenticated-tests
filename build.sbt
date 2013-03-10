name := "play-security-authenticated-tests"

version := "0.1"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings")

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "play" % "play_2.10" % "2.1.0",
  "play" % "play-test_2.10" % "2.1.0",
  "org.specs2" % "specs2_2.10" % "1.13" % "test"
)

