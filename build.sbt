name := "codacy-aligncheck"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "com.codacy" %% "codacy-engine-scala-seed" % "5.0.2",
  "com.github.pathikrit" %% "better-files" % "3.9.1"
).map(_.withSources())

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.3" % Test,
  "org.scalatest" %% "scalatest-wordspec" % "3.2.3" % Test
)

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("com.codacy.tools.aligncheck.Engine")

Universal / javaOptions ++= Seq("-XX:MinRAMPercentage=60.0", "-XX:MaxRAMPercentage=95.0")
