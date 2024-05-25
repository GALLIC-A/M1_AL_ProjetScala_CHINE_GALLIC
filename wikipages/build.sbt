This / scalaVersion := "2.12.15"

coverageEnabled := true
scalafmtOnCompile := true

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.1.0",
  "com.typesafe.play" %% "play-json" % "2.10.5",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.scalatest" %% "scalatest" % "3.2.11" % "test"
)

