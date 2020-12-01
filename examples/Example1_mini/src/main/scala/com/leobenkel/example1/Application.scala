package com.leobenkel.example1

import com.leobenkel.zparkio.Services._
import com.leobenkel.zparkio.ZparkioApp
import com.leobenkel.example1.Application.RuntimeEnv
import com.leobenkel.zparkio.config.scallop.CommandLineArgumentScallop
import izumi.reflect.Tag
import zio.{Has, Task, UIO, ZIO, ZLayer}

trait Application extends ZparkioApp[Arguments, RuntimeEnv, String] {
  implicit lazy final override val tagC:   Tag[Arguments] = Tag.tagFromTagMacro
  implicit lazy final override val tagEnv: Tag[RuntimeEnv] = Tag.tagFromTagMacro

  lazy final override protected val env: ZLayer[ZPARKIO_ENV, Throwable, RuntimeEnv] =
    ZLayer.succeed(())

  lazy final override protected val sparkFactory:  FACTORY_SPARK = SparkBuilder
  lazy final override protected val loggerFactory: FACTORY_LOG = Logger.Factory(Log)
  lazy final override protected val cliFactory: FACTORY_CLI =
    CommandLineArgumentScallop.Factory()
  lazy final override protected val makeConfigErrorParser: ERROR_HANDLER =
    CommandLineArgumentScallop.ErrorParser
  override protected def makeCli(args: List[String]): Arguments = Arguments(args)

  override def runApp(): ZIO[COMPLETE_ENV, Throwable, String] = {
    for {
      s     <- UIO("hello")
      _     <- Logger.info(s"Got: $s")
      a     <- Arguments(_.inputId())
      spark <- SparkModule()
      df    <- Task(spark.sparkContext.parallelize((0 until a).toSeq))
      _     <- Logger.info(s"Count: ${df.count()}")
    } yield { s }
  }

  override def processErrors(f: Throwable): Option[Int] = {
    println(s"Got error: $f")
    f.printStackTrace()
    Some(1)
  }
}

object Application {
  type RuntimeEnv = Has[Unit]
}
