name := "topwords-scala"

version := "0.3"

scalaVersion := "3.3.3"

scalacOptions += "@.scalacOptions.txt"

// Add necessary dependencies
libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"  % "3.2.19"  % Test,
  "org.scalacheck" %% "scalacheck" % "1.18.0"  % Test,
  "org.apache.commons" % "commons-collections4" % "4.4",
  "com.lihaoyi" %% "mainargs" % "0.5.0",
  "org.rogach" %% "scallop" % "5.1.0",
  "org.log4s" %% "log4s" % "1.10.0",
  "org.slf4j" % "slf4j-simple" % "1.7.30"
)

enablePlugins(JavaAppPackaging)
