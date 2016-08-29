import com.typesafe.config._
import com.typesafe.sbt.web.SbtWeb

lazy val conf = ConfigFactory.parseFile(new File("version")).resolve()
lazy val appName = """BusEaAdapter"""
lazy val buildScalaVersion = "2.11.7"
lazy val playVersion = "2.4.3"

lazy val Universal = config("universal")
lazy val IntTest = config("int") extend Test

lazy val testAll = TaskKey[Unit]("test-all")
lazy val testInt = TaskKey[Unit]("test-int")

def intFilter(name: String): Boolean = (name endsWith "IntTest") ||  (name endsWith "IntSpec")
def unitFilter(name: String): Boolean = ((name endsWith "Test") && !intFilter(name)) || ((name endsWith "Spec") && !intFilter(name))

lazy val emberBuild = taskKey[Unit]("Add Ember build to public assets")

def emberCall(param: String = "") = {
  val win = sys.props.get("os.name") exists { _.startsWith("Windows") }
  val emberCmd = if(win) "cmd /C ember.cmd" else "ember"
  val cmd = s"""$emberCmd build --environment="production" """
  scala.sys.process.Process(cmd, new File(s"ui")).!
}

def emberCommand =
  Command.command("ember") { (state) =>
    emberCall()
    state
  }

lazy val emberServer = taskKey[Unit]("run Ember server for development and tests")


lazy val web = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(eaAdapter)
  .configs(IntTest)
  .settings(inConfig(IntTest)(Defaults.testTasks): _*)
  .enablePlugins(SbtWeb)
  .settings(
    name := appName,
    version := "localVersionOfBus",
    scalaVersion := buildScalaVersion,
    testOptions in Test := Seq(Tests.Filter(unitFilter)),
    testOptions in IntTest := Seq(Tests.Filter(intFilter)),
    testInt <<= (test in IntTest),
    testAll <<= (test in IntTest).dependsOn(test in Test),
    scalacOptions := Seq("-unchecked", "-deprecation", "-language:postfixOps"),
    libraryDependencies ++= Seq(
      ws, cache,
      "org.scala-lang" % "scala-compiler" % buildScalaVersion withSources(),
      "org.scala-lang" % "scala-reflect" % buildScalaVersion withSources(),
      "org.springframework.amqp" % "spring-rabbit" % "1.4.4.RELEASE" withSources(),
      "org.quartz-scheduler" % "quartz" % "2.2.1" withSources(),
      "org.apache.httpcomponents" % "httpclient" % "4.0.1" withSources(),
      "org.mongodb" %% "casbah" % "2.8.1",
      "commons-codec" % "commons-codec" % "1.10",
      // for PolarionWSAdapter
      "org.apache.axis" % "axis" % "1.4",
      "org.apache.axis" % "axis-jaxrpc" % "1.4",
      "axis" % "axis-wsdl4j" % "1.5.1",
      "commons-discovery" % "commons-discovery" % "0.5",
      "commons-httpclient" % "commons-httpclient" % "3.1",
      "commons-io" % "commons-io" % "2.4",
      // Test
      "org.scalatestplus" %% "play" % "1.1.0" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
      specs2 % Test,
      "org.mockito" % "mockito-all" % "1.9.5" % "test"
    ),
    resolvers ++= Seq(
      "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
      "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Spring Release Repository" at "https://repo.springsource.org/libs-release",
      "Apache Maven Repo" at "http://central.maven.org/maven2",
      "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
    ),
    routesGenerator := InjectedRoutesGenerator,
    scalaSource in Compile <<= baseDirectory / "dsl",
    doc in Compile <<= target.map(_ / "none"),
    commands += emberCommand,
    emberBuild := {
      emberCall()
      val files = List("vendor.css", "vendor.js", "vendor.map", "ui.css", "ui.js", "ui.map","bootstrap.css.map")
      val copies = (new File("./ui/public/styles/login.css"), new File("./public/styles/login.css")) ::
        files.map { f => (new File(s"./ui/dist/assets/$f"), new File(s"./public/$f"))}

      IO.copy(copies)
    },
    mappings in Universal ++= {
      val nativeDirectory = baseDirectory.value / "native"
      val nativeDirectoryLen = nativeDirectory.getCanonicalPath.length
      val pathFinder = nativeDirectory **  "*"
      pathFinder.get map {
        nativeFile: File =>
          nativeFile -> ("native/" + nativeFile.getCanonicalPath.substring(nativeDirectoryLen))
      }
    },
    mappings in Universal += {
      file("version") -> "version"
    },
    emberServer := {
      emberCall("server")
    }
  )

// lazy val api:Project = (project in file("endpoints/api"))
//   .settings(
//     version := "1.0",
//     scalaVersion := buildScalaVersion,
//     libraryDependencies ++= Seq(
//       "org.scala-lang" % "scala-compiler" % buildScalaVersion withSources(),
//       "org.scala-lang" % "scala-reflect" % buildScalaVersion withSources(),
//       "org.springframework.amqp" % "spring-rabbit" % "1.4.4.RELEASE" withSources(),
//       "com.typesafe.play" %% "play-server" % playVersion,
//       "com.typesafe.play" %% "play-json" % playVersion
//     ),
//     resolvers ++= Seq(
//       "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
//       "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases"
//     )
//   )
//
// lazy val endpointTestSuite: Project = (project in file("endpoints/endpointTestSuite"))
//   .dependsOn(api)
//   .settings(
//     version := "1.0",
//     scalaVersion := buildScalaVersion,
//     libraryDependencies ++= Seq(
//       "com.typesafe.play" %% "play-specs2" % playVersion % Test
//     ),
//     resolvers ++= Seq(
//       "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
//       "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases",
//       "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
//     )
//   )
//
lazy val eaAdapter:Project = (project in file("endpoints/ea-adapter"))
  // .dependsOn(api, endpointTestSuite % "test->test")
  .configs(IntTest)
  .settings(inConfig(IntTest)(Defaults.testTasks): _*)
  .settings(
    version := "1.0",
    scalaVersion := buildScalaVersion,
    unmanagedBase := baseDirectory.value / "lib",
    libraryDependencies ++= Seq(
      // Play parts
      "com.typesafe.play" %% "play-ws" % playVersion,
      // Polarion WS libs, not compatible with Apache CXF
      "org.apache.axis" % "axis" % "1.4",
      "org.apache.axis" % "axis-jaxrpc" % "1.4",
      "axis" % "axis-wsdl4j" % "1.5.1"
    )
  )