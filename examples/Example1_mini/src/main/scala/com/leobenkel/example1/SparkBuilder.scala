package com.leobenkel.example1

import com.leobenkel.zparkio.Services.SparkModule
import org.apache.spark.sql.SparkSession

object SparkBuilder extends SparkModule.Factory[Arguments] {
  override protected def appName: String = "Zparkio_test"

  override protected def updateConfig(
    sparkBuilder: SparkSession.Builder,
    arguments:    Arguments
  ): SparkSession.Builder = {
    sparkBuilder
      .config("spark.foo.bar", arguments.sparkFoo())
  }
}
