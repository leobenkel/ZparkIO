package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import zio.{DefaultRuntime, ZIO}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

// scalastyle:off object.name
object implicits {
  type ZDS_R[R, A] = ZIO[R with SparkModule, Throwable, Dataset[A]]
  type ZDS[A] = ZDS_R[Any, A]

  type ZRDD_R[R, A] = ZIO[R, Throwable, RDD[A]]
  type ZRDD[A] = ZRDD_R[Any, A]

  type ZBC_R[R, A] = ZIO[R with SparkModule, Throwable, Broadcast[A]]
  type ZBC[A] = ZBC_R[Any, A]

  object ZDS {
    def map[A](f: SparkSession => Dataset[A]): ZDS[A] = SparkModule().map(spark => f(spark))

    def flatMap[A](f:     SparkSession => ZDS[A]): ZDS[A] = SparkModule().flatMap(spark => f(spark))
    def flatMapR[R, A](f: SparkSession => ZDS_R[R, A]): ZDS_R[R, A] =
      SparkModule().flatMap(spark => f(spark))

    def apply[A](f: SparkSession => Dataset[A]): ZDS[A] = ZDS.map(f)

    def make[A <: Product: TypeTag: ClassTag, B <: Product: TypeTag: ClassTag](
      input: Dataset[A]
    )(
      f: Dataset[A] => Encoder[B] => Dataset[B]
    ): ZDS[B] = {
      ZDS { spark =>
        f(input)(spark.implicits.newProductEncoder[B])
      }
    }

    def apply[A <: Product: TypeTag: ClassTag](data: A*): ZDS[A] = {
      apply { spark =>
        import spark.implicits._
        data.toDS()
      }
    }

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

  implicit class DatasetZ[R, A](zds: => ZIO[R, Throwable, Dataset[A]]) extends Serializable {
    def zMap[B <: Product: TypeTag: ClassTag](f: A => ZIO[Any, Throwable, B]): ZDS_R[R, B] = {
      ZDS.flatMapR[R, B] { spark =>
        import spark.implicits._
        zds.map { ds =>
          ds.map { a =>
            val zB = f(a)
            val runtime = new DefaultRuntime {}
            runtime.unsafeRun(zB)
          }
        }
      }
    }
  }
}
// scalastyle:on
