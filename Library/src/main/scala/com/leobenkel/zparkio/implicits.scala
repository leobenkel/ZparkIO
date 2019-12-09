package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}
import zio.ZIO

// scalastyle:off object.name
object implicits {
  type ZDS_R[R, A] = ZIO[R with SparkModule, Throwable, Dataset[A]]
  type ZDS[A] = ZDS_R[Any, A]
  type ZRDD[R, A] = ZIO[R, Throwable, RDD[A]]

  object ZDS {
    def apply[A](f: SparkSession => Dataset[A]): ZDS[A] = SparkModule().map(spark => f(spark))

    def apply[A: Encoder](data: Seq[A]): ZDS[A] = {
      apply { spark =>
        import spark.implicits._
        data.toDS()
      }
    }
  }
}
// scalastyle:on
