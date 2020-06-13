package com.leobenkel.zparkioProfileExampleMoreComplex.Services

import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkioProfileExampleMoreComplex.Arguments
import org.apache.spark.sql.SparkSession

object SparkBuilder extends SparkModule.Factory[Arguments] {
  lazy final override protected val appName: String = "Zparkio_test"

  override protected def updateConfig(
    sparkBuilder: SparkSession.Builder,
    arguments:    Arguments
  ): SparkSession.Builder = {
    sparkBuilder
      .config("spark.foo.bar", arguments.sparkConfig())
  }
}
