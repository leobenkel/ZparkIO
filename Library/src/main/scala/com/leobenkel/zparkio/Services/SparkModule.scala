package com.leobenkel.zparkio.Services

import org.apache.spark.sql.SparkSession
import zio.{Task, ZIO}

import scala.util.Try

trait SparkModule {
  def spark: SparkModule.Service
}

object SparkModule {
  def apply(): ZIO[SparkModule, Nothing, SparkSession] =
    ZIO.access[SparkModule](_.spark.spark)

  def getConf(key: String): ZIO[SparkModule, Throwable, String] =
    ZIO
      .access[SparkModule](s => Try(s.spark.spark.conf.get(key)))
      .flatMap(ZIO.fromTry(_))

  trait Service {
    def spark: SparkSession
  }

  trait Builder[C <: CommandLineArguments.Service] {
    lazy private val sparkBuilder:         SparkSession.Builder = SparkSession.builder
    lazy private val sparkBuilderWithName: SparkSession.Builder = sparkBuilder.appName(appName)
    protected def appName: String

    protected def updateConfig(
      sparkBuilder: SparkSession.Builder,
      arguments:    C
    ): SparkSession.Builder = sparkBuilder

    protected def setMaster(sparkBuilder: SparkSession.Builder): SparkSession.Builder =
      sparkBuilder.master("local[*]")

    protected def readyToBuildSparkBuilder(arguments: C): SparkSession.Builder = {
      updateConfig(setMaster(sparkBuilderWithName), arguments)
    }

    protected def makeSparkService(sparkBuilder: SparkSession.Builder): SparkModule.Service = {
      new SparkModule.Service {
        override def spark: SparkSession = sparkBuilder.getOrCreate()
      }
    }

    final def createSpark[R](arguments: C): ZIO[R, Throwable, SparkModule.Service] = {
      Task(makeSparkService(readyToBuildSparkBuilder(arguments)))
    }
  }
}
