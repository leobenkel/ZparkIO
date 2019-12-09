package com.leobenkel.zparkiotest

import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.apache.spark.SparkConf
import org.scalatest.Suite

trait TestWithSpark extends DataFrameSuiteBase { self: Suite =>
  override protected val reuseContextIfPossible: Boolean = true
  override protected val enableHiveSupport:      Boolean = false

  /**
    * To help debug with the Spark UI
    */
  def enableSparkUI: Boolean = {
    false
  }

  final override def conf: SparkConf = {
    if (enableSparkUI) {
      super.conf
        .set("spark.ui.enabled", "true")
        .set("spark.ui.port", "4050")
    } else {
      super.conf
    }
  }
}
