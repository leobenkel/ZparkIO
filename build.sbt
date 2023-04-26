val projectName = IO.readLines(new File("PROJECT_NAME")).head
val v           = IO.readLines(new File("VERSION")).head
val sparkVersions: List[String] = IO.readLines(new File("sparkVersions")).map(_.trim)

val Scala11 = "2.11.12"
val Scala12 = "2.12.17"
val Scala13 = "2.13.10"

val Spark23 = "2.3.4"
val Spark24 = "2.4.8"
val Spark31 = "3.1.3"
val Spark32 = "3.2.3"
val Spark33 = "3.3.1"

val sparkVersionSystem = System.getProperty("sparkVersion", sparkVersions.head)
val sparkVersion       = settingKey[String]("Spark version")

lazy val rootSettings =
  Seq(
    organization       := "com.leobenkel",
    homepage           := Some(url("https://github.com/leobenkel/ZparkIO")),
    licenses           := List("MIT" -> url("https://opensource.org/licenses/MIT")),
    developers         :=
      List(
        Developer(
          "leobenkel",
          "Leo Benkel",
          "",
          url("https://leobenkel.com")
        )
      ),
    sparkVersion       := sparkVersionSystem,
    crossScalaVersions := {
      sparkVersion.value match {
        case Spark23 => Seq(Scala11)
        case Spark24 => Seq(Scala12, Scala11)
        case Spark31 => Seq(Scala12)
        case Spark32 => Seq(Scala13, Scala12)
        case Spark33 => Seq(Scala13, Scala12)
        case s       =>
          throw new Exception(s"crossScalaVersions: Do not know what to do with spark version $s")
      }
    },
    scalaVersion       := crossScalaVersions.value.head,
    resolvers ++= Resolver.sonatypeOssRepos("releases"),
    soteriaAddSemantic := false,
    version ~= (v => s"${sparkVersionSystem}_$v"),
    dynver ~= (v => s"${sparkVersionSystem}_$v")
  )

lazy val zioVersion = "2.0.13"

lazy val commonSettings =
  rootSettings ++
    Seq(
      libraryDependencies ++=
        Seq(
          // https://zio.dev/docs/getting_started.html
          "dev.zio" %% "zio" % zioVersion,

          // SPARK
          "org.apache.spark" %% "spark-core"      % sparkVersion.value,
          "org.apache.spark" %% "spark-streaming" % sparkVersion.value,
          "org.apache.spark" %% "spark-sql"       % sparkVersion.value,
          "org.apache.spark" %% "spark-hive"      % sparkVersion.value,
          "org.apache.spark" %% "spark-catalyst"  % sparkVersion.value,
          "org.apache.spark" %% "spark-yarn"      % sparkVersion.value,
          "org.apache.spark" %% "spark-mllib"     % sparkVersion.value,

          // TEST
          "org.scalatest" %% "scalatest" % "3.2.15" % Test
        ),
      libraryDependencies ++= {
        sparkVersion.value match {
          case Spark23 | Spark24           => Seq(
              "org.apache.xbean" % "xbean-asm6-shaded" % "4.10"
            )
          case Spark31 | Spark32 | Spark33 => Seq(
              "io.netty" % "netty-all"              % "4.1.91.Final",
              "io.netty" % "netty-buffer"           % "4.1.91.Final",
              "io.netty" % "netty-tcnative-classes" % "2.0.60.Final"
            )
          case _                           => Seq.empty
        }
      },
      updateOptions          := updateOptions.value.withGigahorse(false),
      Test / publishArtifact := false,
      pomIncludeRepository   := (_ => false),
      scalacOptions ++= {
        scalaVersion.value match {
          case Scala11 | Scala12 => Seq(
              "-Ywarn-inaccessible",
              "-Ywarn-unused-import"
            )
          case Scala13           => Seq.empty
          case s                 => throw new Exception(s"scalacOptions: Unknown mapping for scala version $s")
        }
      }
      // Can be needed in the future
//      Compile / unmanagedSourceDirectories ++= {
//        val pathWith: String => File = (p: String) => baseDirectory.value / "src" / "main" / p
//        scalaVersion.value match {
//          case Scala13           => Seq(pathWith("scala2.13"))
//          case Scala11 | Scala12 => Seq(pathWith("scala2"))
//          case s                 => throw new Exception(
//              s"unmanagedSourceDirectories: Unknown mapping for scala version $s"
//            )
//        }
//      }
    )

lazy val root = (project in file("."))
  .aggregate(library, testHelper, tests, example1Mini, example2Small)
  .settings(
    name := s"${projectName}_root",
    rootSettings
  )

lazy val library = (project in file("Library")).settings(
  commonSettings,
  name := projectName
)

lazy val sparkTestingBaseVersion: String =
  sparkVersionSystem match {
    // https://mvnrepository.com/artifact/com.holdenkarau/spark-testing-base
    case Spark23 => "2.3.3_0.14.0"
    case Spark24 => "2.4.8_1.3.0"
    case Spark31 => "3.1.2_1.3.0"
    case Spark32 => "3.2.2_1.3.0"
    case Spark33 => "3.3.1_1.4.3"
    case s       => throw new Exception(s"sparkTestingBaseVersion: Unknown mapping for spark version $s")
  }

lazy val testHelper = (project in file("testModules/TestHelper"))
  .settings(
    commonSettings,
    name := s"$projectName-test",
    libraryDependencies ++=
      Seq(
        "com.holdenkarau"  %% "spark-testing-base" % sparkTestingBaseVersion,
        "org.apache.spark" %% "spark-hive"         % sparkVersion.value % Provided
      )
  )
  .dependsOn(library)

lazy val tests = (project in file("testModules/Tests"))
  .settings(
    commonSettings,
    name           := s"${projectName}_tests",
    publish / skip := true
  )
  .dependsOn(
    library,
    testHelper % Test
  )

lazy val libraryConfigsScallop = (project in file("configLibs/Scallop"))
  .settings(
    commonSettings,
    name := s"$projectName-config-scallop",
    libraryDependencies ++=
      Seq(
        // https://github.com/scallop/scallop
        "org.rogach" %% "scallop" % "4.1.0"
      )
  )
  .dependsOn(library)

lazy val example1Mini = (project in file("examples/Example1_mini"))
  .settings(
    commonSettings,
    name                      := s"${projectName}_example1_mini",
    publish / skip            := true,
    assembly / assemblyOption := soteriaAssemblySettings.value
  )
  .enablePlugins(DockerPlugin)
  .dependsOn(
    library,
    libraryConfigsScallop,
    testHelper % Test
  )

lazy val example2Small = (project in file("examples/Example2_small"))
  .settings(
    commonSettings,
    name                      := s"${projectName}_example2_small",
    publish / skip            := true,
    assembly / assemblyOption := soteriaAssemblySettings.value
  )
  .enablePlugins(DockerPlugin)
  .dependsOn(
    library,
    libraryConfigsScallop,
    testHelper % Test
  )

// https://github.com/sbt/sbt/issues/6997#issuecomment-1310637232
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
