name := "chat-app"

version := "0.1"

scalaVersion := "3.6.2"

val Http4sVersion = "0.23.27"
val CirceVersion = "0.14.7"
val LogbackVersion = "1.5.6"
val CatsParseVersion = "1.0.0"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "org.typelevel" %% "cats-parse" % CatsParseVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
)