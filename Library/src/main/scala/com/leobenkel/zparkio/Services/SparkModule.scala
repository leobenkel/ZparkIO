package com.leobenkel.zparkio.Services

import org.apache.spark.sql.SparkSession
import zio.macros.accessible
import zio.{Has, Task, ZIO, ZLayer}

import scala.util.Try

@accessible
object SparkModule {
  type SparkModule = Has[SparkModule.Service]

  def apply(): ZIO[SparkModule, Nothing, SparkSession] = SparkModule.spark

  def getConf(key: String): ZIO[SparkModule, Throwable, String] =
    SparkModule.spark
      .map(s => Try(s.conf.get(key)))
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

    val createSpark: ZLayer[Has[C], Throwable, SparkModule] =
      ZLayer.fromServiceM(createSpark(_))

    final private def createSpark(arguments: C): ZIO[Any, Throwable, SparkModule.Service] = {
      Task(makeSparkService(readyToBuildSparkBuilder(arguments)))
    }
  }
}
