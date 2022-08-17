package com.leobenkel.zparkio.Services

import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import org.apache.spark.sql.SparkSession
import scala.util.Try
import zio.{ZIO, ZLayer}

object SparkModule {
  type SparkModule = SparkModule.Service

  def apply(): ZIO[SparkModule, Throwable, SparkSession] = ZIO.service[SparkModule].map(_.spark)

  def getConf(key: String): ZIO[SparkModule, Throwable, String] =
    SparkModule().map(s => Try(s.conf.get(key))).flatMap(ZIO.fromTry(_))

  trait Service {
    def spark: SparkSession
  }

  trait Factory[C <: CommandLineArguments.Service[C]] {
    lazy private val sparkBuilder:         SparkSession.Builder = SparkSession.builder
    lazy private val sparkBuilderWithName: SparkSession.Builder = sparkBuilder.appName(appName)

    protected def appName: String

    protected def updateConfig(
        sparkBuilder: SparkSession.Builder,
        arguments:    C
    ): SparkSession.Builder = {
      // to silence warning about being unused
      locally(arguments)
      sparkBuilder
    }

    final private def readyToBuildSparkBuilder(
        arguments: C
    ): SparkSession.Builder = updateConfig(sparkBuilderWithName, arguments)

    protected def createSparkSession(
        sparkBuilder: SparkSession.Builder
    ): SparkSession = sparkBuilder.getOrCreate()

    final private def makeSparkService(
        sparkBuilder: SparkSession.Builder
    ): SparkModule.Service =
      new SparkModule.Service {
        lazy final override val spark: SparkSession =
          createSparkSession(
            sparkBuilder
          )
      }

    final private[SparkModule] def createSpark(
        arguments: C
    ): ZIO[Any, Throwable, SparkModule.Service] =
      ZIO.attempt(makeSparkService(readyToBuildSparkBuilder(arguments)))

    private[zparkio] def assembleSparkModule(implicit
        t: zio.Tag[C]
    ): ZLayer[CommandLineArguments[C], Throwable, SparkModule] =
      ZLayer.fromZIO(
        ZIO.serviceWithZIO[CommandLineArguments[C]](c => createSpark(c))
      )
  }
}
