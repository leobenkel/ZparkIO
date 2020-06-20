package com.leobenkel.zparkioProfileExampleMoreComplex

import com.leobenkel.zparkio.Services._
import com.leobenkel.zparkio.ZparkioApp
import com.leobenkel.zparkioProfileExampleMoreComplex.Application.APP_ENV
import com.leobenkel.zparkioProfileExampleMoreComplex.Services.Database.Database
import com.leobenkel.zparkioProfileExampleMoreComplex.Services.FileIO.FileIO
import com.leobenkel.zparkioProfileExampleMoreComplex.Services._
import com.leobenkel.zparkioProfileExampleMoreComplex.Transformations.UserTransformations
import izumi.reflect.Tag
import org.apache.spark.SparkException
import zio.{ZIO, ZLayer}

trait Application extends ZparkioApp[Arguments, APP_ENV, Unit] {
  implicit lazy final override val tagC:   Tag[Arguments] = Tag.tagFromTagMacro
  implicit lazy final override val tagEnv: Tag[APP_ENV] = Tag.tagFromTagMacro

  override protected def env: ZLayer[ZPARKIO_ENV, Throwable, APP_ENV] =
    FileIO.Live ++ Database.Live

  override protected def sparkFactory:  FACTORY_SPARK = SparkBuilder
  override protected def loggerFactory: FACTORY_LOG = Logger.Factory(Log)
  override protected def makeCli(args: List[String]): Arguments = Arguments(args)

  override def runApp(): ZIO[COMPLETE_ENV, Throwable, Unit] = {
    for {
      _       <- Logger.info(s"--Start--")
      authors <- UserTransformations.getAuthors
      _       <- Logger.info(s"There are ${authors.count()} authors")
    } yield ()
  }

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
