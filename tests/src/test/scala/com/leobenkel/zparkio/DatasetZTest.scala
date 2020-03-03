package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkio.implicits.ZDS
import com.leobenkel.zparkiotest.TestWithSpark
import org.apache.spark.sql.SparkSession
import org.scalatest.FreeSpec
import zio.{DefaultRuntime, Task}

// https://stackoverflow.com/a/16990806/3357831
case class TestClass(
  a: Int,
  b: String
)
case class TestClassAfter(a: Int)

class DatasetZTest extends FreeSpec with TestWithSpark {
  "DatasetZ" - {
    import implicits.DatasetZ
    "Test with dataset A " in {
      val s = spark

      val d = ZDS(
        TestClass(a = 1, b = "one"),
        TestClass(a = 2, b = "two"),
        TestClass(a = 3, b = "three")
      ).zMap {
        case TestClass(a, b) => Task(TestClassAfter(a + b.length))
      }

      val r = new DefaultRuntime {}

      assert(
        List(TestClassAfter(4), TestClassAfter(5), TestClassAfter(8)) == r
          .unsafeRun(d.provide(new SparkModule {
            override def spark: SparkModule.Service = new SparkModule.Service {
              override def spark: SparkSession = s
            }
          })).collect().sortBy(_.a).toList
      )
    }
  }
}
