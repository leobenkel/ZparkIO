package com.leobenkel.zparkioProfileExampleMoreComplex

import com.leobenkel.zparkio.Services._
import com.leobenkel.zparkio.ZparkioApp
import com.leobenkel.zparkioProfileExampleMoreComplex.Services.SparkBuilder
import com.leobenkel.zparkioProfileExampleMoreComplex.Transformations.UserTransformations
import org.apache.spark.SparkException
import zio.ZIO

trait Application extends ZparkioApp[Arguments, RuntimeEnv.APP_ENV, Unit] {
  override def runApp(): ZIO[RuntimeEnv.APP_ENV, Throwable, Unit] = {
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

  override def makeEnvironment(
    cliService:   Arguments,
    sparkService: SparkModule.Service
  ): RuntimeEnv.APP_ENV = {
    RuntimeEnv(cliService, sparkService)
  }

  override def makeSparkBuilder: SparkModule.Builder[Arguments] = SparkBuilder

  override def makeCliBuilder: CommandLineArguments.Builder[Arguments] =
    new CommandLineArguments.Builder[Arguments] {
      override protected def createCli(args: List[String]): Arguments = {
        Arguments(args)
      }
    }
}
