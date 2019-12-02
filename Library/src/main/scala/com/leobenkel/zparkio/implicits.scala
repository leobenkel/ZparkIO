package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SparkSession}
import zio.ZIO

// scalastyle:off object.name
object implicits {
  type ZDS_R[R, A] = ZIO[R with SparkModule, Throwable, Dataset[A]]
  type ZDS[A] = ZDS_R[Any, A]
  type ZRDD[R, A] = ZIO[R, Throwable, RDD[A]]

  object ZDS {
    def apply[A](f: SparkSession => Dataset[A]): ZDS[A] = {
      for {
        spark <- SparkModule()
      } yield {
        f(spark)
      }
    }
  }
}
// scalastyle:on
