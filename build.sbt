scalaVersion     := "3.3.6"
organization     := "ice.finance"
organizationName := "ICE"

val fs2Version    = "3.12.0"
val http4sVersion = "0.23.30"
val circeVersion  = "0.14.13"

lazy val root = (project in file("."))
  .settings(
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies ++= List(
      "org.typelevel" %% "cats-core"   % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "co.fs2"        %% "fs2-core"    % fs2Version,
      "co.fs2"        %% "fs2-io"      % fs2Version,

      // Http4s for REST API
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,

      // Circe for JSON handling
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,

      // Logging
      "org.typelevel"       %% "log4cats-slf4j"  % "2.7.1",
      "ch.qos.logback"       % "logback-classic" % "1.5.18",

      //Tests
      "com.disneystreaming" %% "weaver-cats"     % "0.8.3" % Test
    )
  )

Compile / run / fork := true
