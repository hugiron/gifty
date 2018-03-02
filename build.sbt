name := "gifty"

version := "0.1"
scalaVersion := "2.12.4"

val telegramDependencies = Seq(
  "info.mukel" %% "telegrambot4s" % "3.0.14"
)

libraryDependencies ++= telegramDependencies