ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.2"

lazy val root = (project in file("."))
  .settings(
    name := "chat-app"
  )

assembly / mainClass := Some("chatapp.Program")
assembly / assemblyJarName := "chat-app.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

val Http4sVersion = "0.23.27"
val CirceVersion = "0.14.7"
val LogbackVersion = "1.5.6"
val CatsParseVersion = "1.0.0"
val RedisVersion = "1.5.2"
val SkunkVersion = "0.6.3"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "org.typelevel" %% "cats-parse" % CatsParseVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "dev.profunktor" %% "redis4cats-effects" % RedisVersion,
  "dev.profunktor" %% "redis4cats-streams" % RedisVersion,
  "org.tpolecat" %% "skunk-core" % SkunkVersion
)