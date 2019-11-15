package com.leobenkel.zparkioProjectExample

import com.leobenkel.zparkioProjectExample.TestUtils.TestWithSpark
import zio.Exit.{Failure, Success}

class ApplicationTest extends TestWithSpark {
  "Full application" - {
    "Run" in {
      TestApp.unsafeRunSync(TestApp.run("--spark-foo" :: "abc" :: Nil)) match {
        case Success(value) =>
          println(s"Read: $value")
          assertResult(0)(value)
        case Failure(cause) => fail(cause.prettyPrint)
      }
    }

    "Help" in {
      TestApp.unsafeRunSync(TestApp.run("--help" :: Nil)) match {
        case Success(value) =>
          println(s"Read: $value")
          assertResult(0)(value)
        case Failure(cause) => fail(cause.prettyPrint)
      }
    }
  }
}

object TestApp extends Application {}
