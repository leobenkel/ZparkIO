package com.leobenkel.zparkioProfileExampleMoreComplex.Services

import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkioProfileExampleMoreComplex.Arguments
import org.apache.spark.sql.SparkSession

object SparkBuilder extends SparkModule.Builder[Arguments] {
  override protected def appName: String = "Zparkio_test"

  override protected def updateConfig(
    sparkBuilder: SparkSession.Builder,
    arguments:    Arguments
  ): SparkSession.Builder = {
    sparkBuilder
      .config("spark.foo.bar", arguments.sparkConfig())
  }

  override protected def makeSparkService(sparkBuilder: SparkSession.Builder): SparkModule.Service =
    new SparkModule.Service {
      lazy final override val spark: SparkSession = sparkBuilder.getOrCreate
    }
}
