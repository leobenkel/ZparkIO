package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.SparkModule
import org.apache.spark.rdd.{PairRDDFunctions, RDD}
import org.apache.spark.sql._
import zio.ZIO

import scala.reflect.ClassTag

// scalastyle:off object.name
object implicits {
  // TODO: Make the error type more modular

  type ZSpark[A] = ZSpark_R[Any, A]
  type ZSpark_R[R, A] = ZIO[R with SparkModule, Throwable, A]
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

    def make[A: Encoder](input: Seq[A]): ZDS[A] = {
      ZDS.apply { spark =>
        import spark.implicits._
        input.toDS
      }
    }

    def make[A: Encoder](input: A*): ZDS[A] = {
      ZDS.make(input)
    }
  }

  implicit class ZDSUtilsR[R, A](zds: ZDS_R[R, A]) {
    private type RS = R with SparkModule
    def dsMap[RR <: RS, B: Encoder](f: A => B): ZDS_R[RR, B] = zds.map(_.map(f))

    def dsFlatMap[RR <: RS, B: Encoder](f: A => TraversableOnce[B]): ZDS_R[RR, B] =
      zds.map(_.flatMap(f))

    @inline def dsFilter[RR <: RS](p: A => Boolean): ZDS_R[RR, A] = zds.map(_.filter(p))

    @inline def dsRepartition[RR <: RS](numPartitions: Int): ZDS_R[RR, A] =
      zds.map(_.repartition(numPartitions))

    @inline def dsGroupBy[RR <: RS, K: Encoder](
      key: A => K
    ): ZSpark_R[RR, KeyValueGroupedDataset[K, A]] =
      zds.map(_.groupByKey(key))

    private def allJoinsDS[RR <: RS, K, B, D](
      keyBy: A => K
    )(
      other:      Dataset[B],
      otherKeyBy: B => K
    )(
      j: (PairRDDFunctions[K, A], RDD[(K, B)]) => RDD[(K, D)]
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)],
      kd:          Encoder[(K, D)],
      ctk:         ClassTag[K],
      cta:         ClassTag[A]
    ): ZDS_R[RR, (K, D)] = {
      for {
        spark <- SparkModule()
        dsA   <- zds
      } yield {
        import spark.implicits._
        j(
          RDD.rddToPairRDDFunctions(dsA.rdd.keyBy(keyBy(_))),
          other.rdd.keyBy(otherKeyBy(_))
        ).toDS
      }
    }

    def dsJoin[RR <: RS, B, K](
      keyBy: A => K
    )(
      other:      Dataset[B],
      otherKeyBy: B => K
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)],
      kd:          Encoder[(K, (A, B))],
      ctk:         ClassTag[K],
      cta:         ClassTag[A]
    ): ZDS_R[RR, (K, (A, B))] = {
      allJoinsDS(keyBy)(other, otherKeyBy)((rddA, rddB) => rddA.join(rddB))
    }

    def dsLeftOuterJoin[K, B](
      keyBy: A => K
    )(
      other:      Dataset[B],
      otherKeyBy: B => K
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)],
      kd:          Encoder[(K, (A, Option[B]))],
      ctk:         ClassTag[K],
      cta:         ClassTag[A]
    ): ZDS_R[R, (K, (A, Option[B]))] = {
      allJoinsDS[RS, K, B, (A, Option[B])](keyBy)(other, otherKeyBy)(_.leftOuterJoin(_))
    }

    def dsFullOuterJoin[B, K](
      keyBy: A => K
    )(
      other:      Dataset[B],
      otherKeyBy: B => K
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)],
      kd:          Encoder[(K, (Option[A], Option[B]))],
      ctk:         ClassTag[K],
      cta:         ClassTag[A]
    ): ZDS_R[R, (K, (Option[A], Option[B]))] = {
      allJoinsDS[RS, K, B, (Option[A], Option[B])](keyBy)(other, otherKeyBy)(_.fullOuterJoin(_))
    }

    private def allJoins[RR <: RS, K, B, D](
      keyBy: A => K
    )(
      other:      ZDS[B],
      otherKeyBy: B => K
    )(
      j: (RDD[(K, A)], RDD[(K, B)]) => RDD[(K, D)]
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)]
    ): ZDS_R[RR, (K, D)] = {
      for {
        spark <- SparkModule()
        dsA   <- zds.dsMap(a => (keyBy(a), a))
        dsB   <- other.dsMap(b => (otherKeyBy(b), b))
      } yield {
        import spark.implicits._
        j(dsA.rdd, dsB.rdd).toDS
      }
    }

    def dsFlatJoin[RR <: RS, B, K](
      keyBy: A => K
    )(
      other:      ZDS[B],
      otherKeyBy: B => K
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)]
    ): ZDS_R[RR, (K, (A, B))] = {
      allJoins(keyBy)(other, otherKeyBy)(_.join(_))
    }

    def dsFlatLeftOuterJoin[RR <: RS, B, K](
      keyBy: A => K
    )(
      other:      ZDS[B],
      otherKeyBy: B => K
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)]
    ): ZDS_R[RR, (K, (A, Option[B]))] = {
      allJoins(keyBy)(other, otherKeyBy)(_.leftOuterJoin(_))
    }

    def dsFlatFullOuterJoin[RR <: RS, B, K](
      keyBy: A => K
    )(
      other:      ZDS[B],
      otherKeyBy: B => K
    )(
      implicit ka: Encoder[(K, A)],
      kb:          Encoder[(K, B)]
    ): ZDS_R[RR, (K, (Option[A], Option[B]))] = {
      allJoins(keyBy)(other, otherKeyBy)(_.fullOuterJoin(_))
    }
  }

//  implicit class ZDSUtils[A](zds: ZDS[A]) {
//    def dsMap[B: Encoder](f: A => B): ZDS[B] = zds.map(_.map(f))
//
//    def dsFlatMap[B: Encoder](f: A => TraversableOnce[B]): ZDS[B] =
//      zds.map(_.flatMap(f))
//
//    @inline def dsFilter(p: A => Boolean): ZDS[A] = zds.map(_.filter(p))
//
//    @inline def dsRepartition(numPartitions: Int): ZDS[A] =
//      zds.map(_.repartition(numPartitions))
//
//    @inline def dsGroupBy[K: Encoder](key: A => K): ZSpark[KeyValueGroupedDataset[K, A]] =
//      zds.map(_.groupByKey(key))
//
//    private def allJoinsDS[K, B, D](
//      keyBy: A => K
//    )(
//      other:      Dataset[B],
//      otherKeyBy: B => K
//    )(
//      j: (RDD[(K, A)], RDD[(K, B)]) => RDD[(K, D)]
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, D)] = {
//      for {
//        spark <- SparkModule()
//        dsA   <- zds.dsMap[(K, A)](a => (keyBy(a), a))
//      } yield {
//        import spark.implicits._
//        val dsB = other.map((b: B) => (otherKeyBy(b), b))
//        j(dsA.rdd, dsB.rdd).toDS
//      }
//    }
//
//    def dsJoin[B, K](
//      keyBy: A => K
//    )(
//      other:      Dataset[B],
//      otherKeyBy: B => K
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, (A, B))] = {
//      allJoinsDS(keyBy)(other, otherKeyBy)(_.join(_))
//    }
//
//    def dsLeftOuterJoin[B, K](
//      keyBy: A => K
//    )(
//      other:      Dataset[B],
//      otherKeyBy: B => K
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, (A, Option[B]))] = {
//      allJoinsDS(keyBy)(other, otherKeyBy)(_.leftOuterJoin(_))
//    }
//
//    def dsFullOuterJoin[B, K](
//      keyBy: A => K
//    )(
//      other:      Dataset[B],
//      otherKeyBy: B => K
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, (Option[A], Option[B]))] = {
//      allJoinsDS(keyBy)(other, otherKeyBy)(_.fullOuterJoin(_))
//    }
//
//    private def allJoins[K, B, D](
//      keyBy: A => K
//    )(
//      other:      ZDS[B],
//      otherKeyBy: B => K
//    )(
//      j: (RDD[(K, A)], RDD[(K, B)]) => RDD[(K, D)]
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, D)] = {
//      for {
//        spark <- SparkModule()
//        dsA   <- zds.dsMap(a => (keyBy(a), a))
//        dsB   <- other.dsMap(b => (otherKeyBy(b), b))
//      } yield {
//        import spark.implicits._
//        j(dsA.rdd, dsB.rdd).toDS
//      }
//    }
//
//    def dsFlatJoin[B, K](
//      keyBy: A => K
//    )(
//      other:      ZDS[B],
//      otherKeyBy: B => K
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, (A, B))] = {
//      allJoins(keyBy)(other, otherKeyBy)(_.join(_))
//    }
//
//    def dsFlatLeftOuterJoin[B, K](
//      keyBy: A => K
//    )(
//      other:      ZDS[B],
//      otherKeyBy: B => K
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, (A, Option[B]))] = {
//      allJoins(keyBy)(other, otherKeyBy)(_.leftOuterJoin(_))
//    }
//
//    def dsFlatFullOuterJoin[B, K](
//      keyBy: A => K
//    )(
//      other:      ZDS[B],
//      otherKeyBy: B => K
//    )(
//      implicit ka: Encoder[(K, A)],
//      kb:          Encoder[(K, B)]
//    ): ZDS[(K, (Option[A], Option[B]))] = {
//      allJoins(keyBy)(other, otherKeyBy)(_.fullOuterJoin(_))
//    }
//  }
}
// scalastyle:on
