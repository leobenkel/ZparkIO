package com.leobenkel.example1

import com.leobenkel.zparkiotest.TestWithSpark
import org.scalatest.freespec.AnyFreeSpec
import zio.Exit.{Failure, Success}
import zio.{Clock, Console, Random, Runtime, System, Unsafe, ZIO}

class ApplicationTest extends AnyFreeSpec with TestWithSpark {
  "Full application - Example 1" - {
    "Run" in {
      Unsafe.unsafe {implicit unsafe =>
        TestApp
          .makeRuntime
          .unsafe
          .run(
            TestApp.runTest("--spark-foo" :: "abc" :: Nil)
          ) match {
          case Success(value) =>
            println(s"Read: $value")
            assertResult(0)(value)
          case Failure(cause) => fail(cause.prettyPrint)
        }
      }
    }

    "Wrong argument" in {
      Unsafe.unsafe { implicit unsafe =>
        TestApp
          .makeRuntime
          .unsafe
          .run(
            TestApp.runTest("--bar" :: "foo" :: Nil)
          ) match {
          case Success(value) =>
            println(s"Read: $value")
            assertResult(1)(value)
          case Failure(cause) => fail(cause.prettyPrint)
        }
      }
    }

    "Help" in {
      Unsafe.unsafe {implicit unsafe =>
        TestApp.makeRuntime.unsafe.run(TestApp.runTest("--help" :: Nil)) match {
          case Success(value) =>
            println(s"Read: $value")
            assertResult(0)(value)
          case Failure(cause) => fail(cause.prettyPrint)
        }
      }
    }
  }
}

object TestApp extends Application {
  def runTest(args: List[String]): ZIO[Clock with Console with System with Random, Throwable, Int] =
    super.run(args)

  lazy final override val makeRuntime: Runtime[Clock with Console with System with Random] =
    super.makeRuntime
}
