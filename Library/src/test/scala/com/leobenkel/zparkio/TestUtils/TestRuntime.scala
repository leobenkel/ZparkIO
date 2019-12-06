package com.leobenkel.zparkio.TestUtils
import com.leobenkel.zparkio.Services.{Logger, SparkModule}
import org.apache.spark.sql.SparkSession
import zio.console.Console
import zio.internal.{Platform, PlatformLive}
import zio.{Runtime, UIO, ZIO}

case class TestRuntime(sparkSession: SparkSession) extends Runtime[TestRuntime.EnvTest] {
  override val Environment: TestRuntime.EnvTest = TestEnvironment(sparkSession)
  val Platform:             Platform = PlatformLive.Default
}

object TestRuntime {
  type EnvTest = Any with Logger with SparkModule
}

case class TestEnvironment(sparkSession: SparkSession) extends Logger with SparkModule {
  override def log: Logger.Service = LoggerTest()

  override def spark: SparkModule.Service = new SparkModule.Service {
    override def spark: SparkSession = sparkSession
  }
}

case class LoggerTest() extends Logger.Service {
  override def info(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"INFO: $txt"))
  override def error(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"ERROR: $txt"))
  override def debug(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"DEBUG: $txt"))
}
