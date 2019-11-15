package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.CommandLineArguments.HelpHandlerException
import com.leobenkel.zparkio.Services.{CommandLineArguments, Logger, SparkModule}
import zio.{App, Task, ZIO}

trait ZparkioApp[C <: CommandLineArguments.Service, ENV <: ZparkioApp.ZPEnv, OUTPUT] extends App {

  def makeSparkBuilder: SparkModule.Builder[C]
  def makeCliBuilder:   CommandLineArguments.Builder[C]

  def runApp(): ZIO[ENV, Throwable, OUTPUT]
  def makeEnvironment(
    cliService:   CommandLineArguments.Service,
    sparkService: SparkModule.Service
  ): ENV

  def processErrors(f: Throwable): Int = 1

  private def buildEnv(args: List[String]): ZIO[zio.ZEnv, Throwable, ENV] = {
    for {
      cliBuilder   <- Task(makeCliBuilder)
      cliService   <- cliBuilder.createCliSafely(args)
      sparkBuilder <- Task(makeSparkBuilder)
      sparkService <- sparkBuilder.createSpark(cliService)
    } yield { makeEnvironment(cliService, sparkService) }
  }

  private def app(args: List[String]): ZIO[zio.ZEnv, Throwable, OUTPUT] = {
    for {
      env    <- buildEnv(args)
      output <- runApp().provide(env)
    } yield { output }
  }

  final override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    app(args)
      .catchSome { case h: HelpHandlerException => h.printHelpMessage }
      .fold({
        case CommandLineArguments.ErrorParser(code) => code
        case e                                      => processErrors(e)
      }, _ => 0)
  }
}

object ZparkioApp {
  type ZPEnv = zio.ZEnv with CommandLineArguments with Logger with SparkModule
}
