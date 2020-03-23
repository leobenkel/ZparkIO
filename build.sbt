val projectName = IO.readLines(new File("PROJECT_NAME")).head
val v = IO.readLines(new File("VERSION")).head

val sparkVersion = "2.3.1"

lazy val commonSettings = Seq(
  organization := "com.leobenkel",
  homepage     := Some(url("https://github.com/leobenkel/Sparkio")),
  licenses     := List("MIT" -> url("https://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      "leobenkel",
      "Leo Benkel",
      "",
      url("https://leobenkel.com")
    )
  ),
  scalaVersion := "2.11.12",
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    // https://zio.dev/docs/getting_started.html
    "dev.zio" %% "zio" % "1.0.0-RC16",
    // https://github.com/scallop/scallop
    "org.rogach" %% "scallop" % "3.3.1",
    // https://mvnrepository.com/artifact/org.apache.spark/spark-core
    "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
    // https://mvnrepository.com/artifact/org.apache.spark/spark-sql
    "org.apache.spark" %% "spark-sql"          % sparkVersion              % Provided,
    "com.holdenkarau"  %% "spark-testing-base" % s"${sparkVersion}_0.10.0" % Test,
    "org.apache.spark" %% "spark-hive"         % sparkVersion              % Test
  ),
  logLevel in stryker     := Level.Debug,
  updateOptions           := updateOptions.value.withGigahorse(false),
  publishArtifact in Test := false,
  pomIncludeRepository    := (_ => false)
)

lazy val root = (project in file("."))
  .aggregate(library, testHelper, tests, projectExample, projectExampleMoreComplex)
  .settings(
    name := s"$projectName-$v"
  )

lazy val library = (project in file("Library"))
  .settings(
    commonSettings,
    name := projectName
  )

lazy val testHelper = (project in file("TestHelper"))
  .settings(
    commonSettings,
    name := s"$projectName-test",
    libraryDependencies ++= Seq(
      "com.holdenkarau"  %% "spark-testing-base" % s"${sparkVersion}_0.10.0",
      "org.apache.spark" %% "spark-hive"         % sparkVersion % Provided
    )
  )
  .dependsOn(library)

lazy val tests = (project in file("tests"))
  .settings(
    commonSettings,
    name           := s"${projectName}_tests",
    publish / skip := true
  )
  .dependsOn(
    library    % Test,
    testHelper % Test
  )

lazy val projectExample = (project in file("ProjectExample"))
  .settings(
    commonSettings,
    name                       := s"${projectName}_testProject",
    publish / skip             := true,
    assemblyOption in assembly := safetyAssemblySettings.value
  ).enablePlugins(DockerPlugin)
  .dependsOn(library, testHelper % Test)

lazy val projectExampleMoreComplex = (project in file("ProjectExample_MoreComplex"))
  .settings(
    commonSettings,
    name                       := s"${projectName}_testProject_moreComplex",
    publish / skip             := true,
    assemblyOption in assembly := safetyAssemblySettings.value
  ).enablePlugins(DockerPlugin)
  .dependsOn(library, testHelper % Test)
