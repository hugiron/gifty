name := "gifty"

version := "0.1"
scalaVersion := "2.11.12"

val telegramDependencies = Seq(
  "info.mukel" %% "telegrambot4s" % "3.0.14"
)

val storageDependencies = Seq(
  "net.debasishg" %% "redisreact" % "0.9",
  "org.postgresql" % "postgresql" % "42.2.1",
  "com.typesafe.slick" %% "slick" % "3.2.1"
)

val nd4sDependencies = Seq(
  "org.nd4j" % "nd4j-native-platform" % "0.9.1",
  "org.nd4j" %% "nd4s" % "0.9.1"
)

libraryDependencies ++= (
  telegramDependencies ++
  storageDependencies ++
  nd4sDependencies
)