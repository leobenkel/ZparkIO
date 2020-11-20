package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkio.Services.SparkModule.SparkModule
import com.leobenkel.zparkio.implicits.{ZDS, ZDS_R}
import com.leobenkel.zparkiotest.TestWithSpark
import org.apache.spark.sql.SparkSession
import org.scalatest.freespec.AnyFreeSpec
import zio.{BootstrapRuntime, Task, ZLayer}

// https://stackoverflow.com/a/16990806/3357831
case class TestClass(
  a: Int,
  b: String
)
case class TestClassAfter(a: Int)

class DatasetZTest extends AnyFreeSpec with TestWithSpark {
  "DatasetZ" - {
    import implicits.DatasetZ
    "Test with dataset A " in {
      val s = spark

      val d: ZDS_R[SparkModule, TestClassAfter] = ZDS(
        TestClass(a = 1, b = "one"),
        TestClass(a = 2, b = "two"),
        TestClass(a = 3, b = "three")
      ).zMap {
        case TestClass(a, b) =>
          Task(TestClassAfter(a + b.length))
      }

      val r = new BootstrapRuntime {}

      assert(
        List(TestClassAfter(4), TestClassAfter(5), TestClassAfter(8)) == r
          .unsafeRun(d.provideLayer(ZLayer.succeed(new SparkModule.Service {
            override def spark: SparkSession = s
          }))).collect().sortBy(_.a).toList
      )
    }

    "Test for mapDS" in {
      val s = spark

      val d: ZDS_R[SparkModule, TestClassAfter] = ZDS(
        TestClass(a = 1, b = "one"),
        TestClass(a = 2, b = "two"),
        TestClass(a = 3, b = "three")
      ).mapDS {
        case TestClass(a, b) =>
          TestClassAfter(a + b.length)
      }

      val r = new BootstrapRuntime {}

      assert(
        List(TestClassAfter(4), TestClassAfter(5), TestClassAfter(8)) == r
          .unsafeRun(d.provideLayer(ZLayer.succeed(new SparkModule.Service {
            override def spark: SparkSession = s
          }))).collect().sortBy(_.a).toList
      )
    }
  }
}
