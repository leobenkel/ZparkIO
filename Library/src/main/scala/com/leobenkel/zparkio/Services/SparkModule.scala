package com.leobenkel.zparkio.Services

import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import org.apache.spark.sql.SparkSession
import zio.{Has, Tag, Task, ZIO, ZLayer}

import scala.util.Try

object SparkModule {
  type SparkModule = Has[SparkModule.Service]

  def apply(): ZIO[SparkModule, Nothing, SparkSession] = ZIO.access[SparkModule](_.get.spark)

  def getConf(key: String): ZIO[SparkModule, Throwable, String] =
    SparkModule()
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

    final private def readyToBuildSparkBuilder(arguments: C): SparkSession.Builder = {
      updateConfig(setMaster(sparkBuilderWithName), arguments)
    }

    final private def makeSparkService(sparkBuilder: SparkSession.Builder): SparkModule.Service = {
      new SparkModule.Service {
        lazy final override val spark: SparkSession = sparkBuilder.getOrCreate()
      }
    }

    final private[SparkModule] def createSpark(
      arguments: C
    ): ZIO[Any, Throwable, SparkModule.Service] = {
      Task(makeSparkService(readyToBuildSparkBuilder(arguments)))
    }
  }

  trait Factory[C <: CommandLineArguments.Service] {
    protected[Factory] def makeSparkModule: ZIO[Any, Throwable, SparkModule.Builder[C]]

    protected def assembleSparkModule(implicit t: Tag[C]): ZLayer[CommandLineArguments[C], Throwable, SparkModule] =
      ZLayer.fromServiceM(cli => makeSparkModule.flatMap(_.createSpark(cli)))
  }

  def make[C <: CommandLineArguments.Service](
    builder: Builder[C]
  )(implicit t: Tag[C]): ZLayer[Has[C], Throwable, SparkModule] = ZLayer.fromServiceM(builder.createSpark)

}
