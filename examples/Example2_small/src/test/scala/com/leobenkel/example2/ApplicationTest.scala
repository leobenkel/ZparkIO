package com.leobenkel.example2

import com.leobenkel.zparkio.Services.Logger
import com.leobenkel.example2.Application.APP_ENV
import com.leobenkel.example2.Items.{Post, User}
import com.leobenkel.example2.Services.{Database, FileIO}
import com.leobenkel.zparkiotest.{LoggerService, TestWithSpark}
import org.apache.spark.sql._
import org.scalatest.freespec.AnyFreeSpec
import zio.Exit.{Failure, Success}
import zio.console.Console
import zio.{BootstrapRuntime, Task, ZIO, ZLayer}

class ApplicationTest extends AnyFreeSpec with TestWithSpark {
  "Full application - Example 2" - {
    "Run" in {
      val testApp = TestApp(spark)
      testApp.makeRuntime.unsafeRunSync(testApp.runTest(Nil)) match {
        case Success(value) =>
          println(s"Read exit code: $value")
          assertResult(0)(value)
        case Failure(cause) => fail(cause.prettyPrint)
      }
    }

    "Wrong argument" in {
      val testApp = TestApp(spark)
      testApp.makeRuntime.unsafeRunSync(testApp.runTest("--bar" :: "foo" :: Nil)) match {
        case Success(value) =>
          println(s"Read: $value")
          assertResult(1)(value)
        case Failure(cause) => fail(cause.prettyPrint)
      }
    }

    "Help" in {
      val testApp = TestApp(spark)
      testApp.makeRuntime.unsafeRunSync(testApp.runTest("--help" :: Nil)) match {
        case Success(value) =>
          println(s"Read exit code: $value")
          assertResult(0)(value)
        case Failure(cause) => fail(cause.prettyPrint)
      }
    }
  }
}

case class TestApp(s: SparkSession) extends Application {
  def runTest(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = super.run(args)

  override def makeRuntime: BootstrapRuntime = super.makeRuntime

  override protected def sparkFactory: FACTORY_SPARK =
    new FACTORY_SPARK {
      lazy final override protected val appName: String = "Test"

      final override protected def createSparkSession(
        sparkBuilder: SparkSession.Builder
      ): SparkSession = s
    }

  override protected def loggerFactory: FACTORY_LOG =
    new FACTORY_LOG {
      override protected def makeLogger(
        console: Console.Service
      ): ZIO[Any, Throwable, Logger.Service] = {
        Task(new LoggerService {})
      }
    }

  lazy final override protected val env: ZLayer[ZPARKIO_ENV, Throwable, APP_ENV] = {
    FileIO.Live ++ ZLayer.succeed {
      new Database.Service {
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
  }

}
