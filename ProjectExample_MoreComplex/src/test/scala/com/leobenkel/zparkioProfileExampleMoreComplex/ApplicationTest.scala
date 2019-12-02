package com.leobenkel.zparkioProfileExampleMoreComplex

import com.leobenkel.zparkio.Services.{CommandLineArguments, Logger, SparkModule}
import com.leobenkel.zparkioProfileExampleMoreComplex.Items.{Post, User}
import com.leobenkel.zparkioProfileExampleMoreComplex.Services.{Database, FileIO}
import com.leobenkel.zparkioProfileExampleMoreComplex.TestUtils.TestWithSpark
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}
import zio.Exit.{Failure, Success}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.system.System

class ApplicationTest extends TestWithSpark {
  "Full application" - {
    "Run" in {
      TestApp.unsafeRunSync(TestApp.run(Nil)) match {
        case Success(value) =>
          println(s"Read exit code: $value")
          assertResult(0)(value)
        case Failure(cause) => fail(cause.prettyPrint)
      }
    }

    "Help" in {
      TestApp.unsafeRunSync(TestApp.run("--help" :: Nil)) match {
        case Success(value) =>
          println(s"Read exit code: $value")
          assertResult(0)(value)
        case Failure(cause) => fail(cause.prettyPrint)
      }
    }
  }
}

object TestApp extends Application {
  override def makeEnvironment(
    cliService:   Arguments,
    sparkService: SparkModule.Service
  ): RuntimeEnv.APP_ENV = {
    TestEnv(cliService, sparkService)
  }
}

case class TestEnv(
  cliService:   Arguments,
  sparkService: SparkModule.Service
) extends System.Live with Console.Live with Clock.Live with Random.Live with Blocking.Live
    with CommandLineArguments[Arguments] with Logger with FileIO.Live with SparkModule
    with Database {
  lazy final override val cli:   Arguments = cliService
  lazy final override val spark: SparkModule.Service = sparkService
  lazy final override val log:   Logger.Service = new Log()

  override def database: Database.Service = new Database.Service {
    override protected def query[A: Encoder](
      spark: SparkSession,
      query: String
    ): Dataset[A] = {
      val rawSeq = query match {
        case "SELECT * FROM users" =>
          Seq[User](
            User(
              userId = 1,
              name = "Leo",
              age = 30,
              active = true
            )
          )
        case "SELECT * FROM posts" =>
          Seq[Post](
            Post(
              postId = 5,
              authorId = 1,
              title = "Foo",
              content = "Bar"
            )
          )
        case q => throw new UnsupportedOperationException(q)
      }

      import spark.implicits._
      rawSeq.map(_.asInstanceOf[A]).toDS
    }
  }
}
