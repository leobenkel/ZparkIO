package com.leobenkel.example2

import com.leobenkel.example2.Application.APP_ENV
import com.leobenkel.example2.Services._
import com.leobenkel.example2.Services.Database.Database
import com.leobenkel.example2.Services.FileIO.FileIO
import com.leobenkel.example2.Transformations.UserTransformations
import com.leobenkel.zparkio.Services._
import com.leobenkel.zparkio.ZparkioApp
import com.leobenkel.zparkio.config.scallop.CommandLineArgumentScallop
import izumi.reflect.Tag
import org.apache.spark.SparkException
import zio.{ZIO, ZLayer}

trait Application extends ZparkioApp[Arguments, APP_ENV, Unit] {
  implicit lazy final override val tagC:   zio.Tag[Arguments] = zio.Tag(Tag.tagFromTagMacro)
  implicit lazy final override val tagEnv: zio.Tag[APP_ENV]   = zio.Tag(Tag.tagFromTagMacro)

  override protected def env: ZLayer[ZPARKIO_ENV, Throwable, APP_ENV] = FileIO.Live ++ Database.Live

  override protected def sparkFactory:  FACTORY_SPARK = SparkBuilder
  override protected def loggerFactory: FACTORY_LOG   = Logger.Factory(Log)
  override protected def makeCli(args: List[String]): Arguments = Arguments(args)
  lazy final override protected val cliFactory:            FACTORY_CLI   = CommandLineArgumentScallop.Factory()
  lazy final override protected val makeConfigErrorParser: ERROR_HANDLER =
    CommandLineArgumentScallop.ErrorParser

  override def runApp(): ZIO[COMPLETE_ENV, Throwable, Unit] =
    for {
      _       <- Logger.info(s"--Start--")
      authors <- UserTransformations.getAuthors
      _       <- Logger.info(s"There are ${authors.count()} authors")
    } yield ()

  override def processErrors(f: Throwable): Option[Int] = {
    println(f)
    f.printStackTrace(System.out)

    f match {
      case _: SparkException       => Some(10)
      case _: InterruptedException => Some(0)
      case _ => Some(1)
    }
  }
}

object Application {
  type APP_ENV = FileIO with Database
}
