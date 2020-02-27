package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import zio.ZIO

import scala.reflect.ClassTag

// scalastyle:off object.name
object implicits {
  type ZDS_R[R, A] = ZIO[R with SparkModule, Throwable, Dataset[A]]
  type ZDS[A] = ZDS_R[Any, A]

  type ZRDD_R[R, A] = ZIO[R, Throwable, RDD[A]]
  type ZRDD[A] = ZRDD_R[Any, A]

  type ZBC_R[R, A] = ZIO[R with SparkModule, Throwable, Broadcast[A]]
  type ZBC[A] = ZBC_R[Any, A]

  object ZDS {
    def apply[A](f: SparkSession => Dataset[A]): ZDS[A] = SparkModule().map(spark => f(spark))

    def apply[A: Encoder](data: Seq[A]): ZDS[A] = {
      apply { spark =>
        import spark.implicits._
        data.toDS()
      }
    }

    def broadcast[A: ClassTag](f: SparkSession => A): ZBC[A] = {
      SparkModule().map(spark => spark.sparkContext.broadcast(f(spark)))
    }
  }
}
// scalastyle:on
