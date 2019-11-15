package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.CommandLineArguments.HelpHandlerException
import com.leobenkel.zparkio.Services._
import zio.duration.Duration
import zio.{App, Task, UIO, ZIO}

trait ZparkioApp[C <: CommandLineArguments.Service, ENV <: ZparkioApp.ZPEnv[C], OUTPUT]
    extends App {

  def makeSparkBuilder: SparkModule.Builder[C]
  def makeCliBuilder:   CommandLineArguments.Builder[C]
  protected def displayCommandLines: Boolean = true

  def runApp(): ZIO[ENV, Throwable, OUTPUT]
  def makeEnvironment(
    cliService:   C,
    sparkService: SparkModule.Service
  ): ENV

  protected def processErrors(f: Throwable): Option[Int] = Some(1)
  protected def timedApplication: Duration = Duration.Infinity

  object ErrorProcessing {
    def unapply(e: Throwable): Option[Int] = {
      processErrors(e)
    }
  }

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
      env <- buildEnv(args)
      _ <- if (displayCommandLines) {
        CommandLineArguments.displayCommandLines().provide(env)
      } else {
        UIO(())
      }
      output <- runApp()
        .provide(env)
        .timeoutFail(ZparkioApplicationTimeoutException())(timedApplication)
    } yield { output }
  }

  final override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    app(args)
      .catchSome { case h: HelpHandlerException => h.printHelpMessage }
      .fold(
        {
          case CommandLineArguments.ErrorParser(code) => code
          case ErrorProcessing(errorCode)             => errorCode
          case _                                      => 1
        },
        _ => 0
      )
  }
}

object ZparkioApp {
  type ZPEnv[C <: CommandLineArguments.Service] =
    zio.ZEnv with CommandLineArguments[C] with Logger with SparkModule
}
