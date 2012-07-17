name := "play-casbah"

version := "0.1"

scalaVersion := "2.9.1"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= Seq(
    "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
    "play" %% "play" % "2.0.2",
    "play" %% "play-test" % "2.0.2" % "test",
    "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test"
)
