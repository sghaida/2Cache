lazy val root = (project in file(".")).settings(
  name := "2Cache",
  version := "0.1",
  scalaVersion := "2.12.8",
  logLevel := Level.Warn,

  mainClass in Compile := Some("com.sghaida.engine.mainApp"),

  assemblyJarName := s"${name.value}-${version.value}.jar",

  scalacOptions ++= Seq(
    "-feature",
    "-language:implicitConversions",
    "-language:postfixOps"
  )
)

val akkaVersion = "2.5.21"

libraryDependencies ++= Seq(
  "com.typesafe.akka"       %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"       %% "akka-slf4j"           % akkaVersion,
  "org.slf4j"               % "slf4j-simple"          % "1.7.5",
  "com.github.nscala-time"  %% "nscala-time"          % "2.18.0",
  "com.typesafe.akka"       %% "akka-testkit"         % akkaVersion % Test,
  "org.scalatest"           %% "scalatest"            % "3.0.6" % Test
)