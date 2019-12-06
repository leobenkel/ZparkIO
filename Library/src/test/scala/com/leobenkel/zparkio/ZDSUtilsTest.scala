package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkio.TestUtils.{TestRuntime, TestWithSpark}
import com.leobenkel.zparkio.implicits.ZDS

class ZDSUtilsTest extends TestWithSpark {
  "Test ZDSUtils" - {
    import com.leobenkel.zparkio.implicits._
    val runtime = TestRuntime(spark)
    "Test map" - {
      "Test 1" in {
        val job = SparkModule()
          .flatMap { spark =>
            import spark.implicits._
            val a: ZDS[(Int, String)] = ZDS
              .make(1 until 100)
              .dsMap(_ * 2)
              .dsMap(i => (i, i.toString.reverse))

            val b: ZDS[(Int, Char)] = ZDS
              .make(1 until 200 by 2)
              .dsFilter(_ % 3 == 0)
              .dsMap(i => (i, i.toChar))

            a.dsFlatJoin(_._1)(b, (bb: (Int, Char)) => bb._1)
          }
          .map(_.show(100, truncate = false))

        println(runtime.unsafeRunSync(job))
      }

    }

  }
}
