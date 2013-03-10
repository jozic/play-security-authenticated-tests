name := "play-security-authenticated-tests"

version := "0.1"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings")

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "play" %% "play" % "2.0.3",
  "play" %% "play-test" % "2.0.3",
  "org.specs2" %% "specs2" % "1.9" % "test"
)

