val projectName = IO.readLines(new File("PROJECT_NAME")).head
val v = IO.readLines(new File("VERSION")).head
val sparkVersions: List[String] = IO.readLines(new File("sparkVersions")).map(_.trim)

val scala11 = "2.11.12"
val scala12 = "2.12.12"
val sparkVersionSystem = System.getProperty("sparkVersion", sparkVersions.head)
val sparkVersion = settingKey[String]("Spark version")

lazy val rootSettings = Seq(
  organization := "com.leobenkel",
  homepage     := Some(url("https://github.com/leobenkel/ZparkIO")),
  licenses     := List("MIT" -> url("https://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      "leobenkel",
      "Leo Benkel",
      "",
      url("https://leobenkel.com")
    )
  ),
  sparkVersion := sparkVersionSystem,
  crossScalaVersions := {
    sparkVersion.value match {
      case "2.3.3" => Seq(scala11)
      case "2.4.5" => Seq(scala11, scala12)
    }
  },
  scalaVersion := crossScalaVersions.value.head,
  resolvers += Resolver.sonatypeRepo("releases"),
  soteriaAddSemantic := false,
  version ~= (v => s"${sparkVersionSystem}_$v"),
  dynver ~= (v => s"${sparkVersionSystem}_$v")
)

lazy val zioVersion = "1.0.3"

lazy val commonSettings = rootSettings ++ Seq(
  libraryDependencies ++= Seq(
    // https://zio.dev/docs/getting_started.html
    "dev.zio" %% "zio" % zioVersion,
    // https://github.com/scallop/scallop
    "org.rogach" %% "scallop" % "3.5.1",
    // https://mvnrepository.com/artifact/org.apache.spark/spark-core
    "org.apache.spark" %% "spark-core" % sparkVersion.value % Provided,
    // https://mvnrepository.com/artifact/org.apache.spark/spark-sql
    "org.apache.spark" %% "spark-sql" % sparkVersion.value % Provided,
    "org.scalatest"    %% "scalatest" % "3.2.2"            % Test
  ),
  logLevel in stryker     := Level.Debug,
  updateOptions           := updateOptions.value.withGigahorse(false),
  publishArtifact in Test := false,
  pomIncludeRepository    := (_ => false)
)

lazy val root = (project in file("."))
  .aggregate(library, testHelper, tests, projectExample, projectExampleMoreComplex)
  .settings(
    name := s"${projectName}_root",
    rootSettings
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
      "com.holdenkarau"  %% "spark-testing-base" % s"${sparkVersion.value}_0.14.0",
      "org.apache.spark" %% "spark-hive"         % sparkVersion.value % Provided
    )
  )
  .dependsOn(library)

lazy val tests = (project in file("Tests"))
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
    assemblyOption in assembly := soteriaAssemblySettings.value
  ).enablePlugins(DockerPlugin)
  .dependsOn(library, testHelper % Test)

lazy val projectExampleMoreComplex = (project in file("ProjectExample_MoreComplex"))
  .settings(
    commonSettings,
    name                       := s"${projectName}_testProject_moreComplex",
    publish / skip             := true,
    assemblyOption in assembly := soteriaAssemblySettings.value
  ).enablePlugins(DockerPlugin)
  .dependsOn(library, testHelper % Test)
