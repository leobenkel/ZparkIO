package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.CommandLineArguments.HelpHandlerException
import com.leobenkel.zparkio.Services.{CommandLineArguments => CLA, _}
import org.rogach.scallop.exceptions.ScallopException
import zio.console.Console
import zio.duration.Duration
import zio.internal.{Platform, PlatformLive}
import zio.{DefaultRuntime, Task, UIO, ZIO}

trait ZparkioApp[C <: CLA.Service, ENV <: ZparkioApp.ZPEnv[C] with Logger, OUTPUT] {
  protected def makeSparkBuilder: SparkModule.Builder[C]
  protected def makeCliBuilder:   CLA.Builder[C]
  protected def displayCommandLines: Boolean = true
  protected def makeLogger: Logger

  protected def makeEnvironment(
    cliService:    C,
    loggerService: Logger.Service,
    sparkService:  SparkModule.Service
  ):                      ENV
  protected def runApp(): ZIO[ENV, Throwable, OUTPUT]

  protected def processErrors(f: Throwable): Option[Int] = Some(1)
  protected def timedApplication: Duration = Duration.Infinity

  protected def makePlatform: Platform = {
    PlatformLive.Default
      .withReportFailure { cause =>
        if (cause.died) println(cause.prettyPrint)
      }
  }

  def makeRuntime: DefaultRuntime = new DefaultRuntime {
    override val Platform: Platform = makePlatform
  }

  private object ErrorProcessing {
    def unapply(e: Throwable): Option[Int] = {
      processErrors(e)
    }
  }

  protected def buildEnv(args: List[String]): ZIO[zio.ZEnv, Throwable, ENV] = {
    for {
      c          <- ZIO.environment[Console]
      logger     <- Task(makeLogger)
      cliBuilder <- Task(makeCliBuilder)
      cliService <- cliBuilder.createCliSafely(args).tapError {
        case cliError: ScallopException =>
          Logger
            .displayAllErrors(cliError).provide(new Logger with Console {
              lazy final override val log:     Logger.Service = logger.log
              lazy final override val console: Console.Service[Any] = c.console
            })
        case _ => UIO(())
      }
      sparkBuilder <- Task(makeSparkBuilder)
      sparkService <- sparkBuilder.createSpark(cliService)
    } yield { makeEnvironment(cliService, logger.log, sparkService) }
  }

  protected def app(args: List[String]): ZIO[zio.ZEnv, Throwable, OUTPUT] = {
    for {
      env <- buildEnv(args)
      s   <- SparkModule().provide(env)
      _ <- if (displayCommandLines) {
        CLA.displayCommandLines().provide(env)
      } else {
        UIO(())
      }
      output <- runApp()
        .provide(env)
        .timeoutFail(ZparkioApplicationTimeoutException())(timedApplication)
      _ = s.sparkContext.stop()
      _ = s.stop()
    } yield { output }
  }

  protected def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    app(args)
      .catchSome { case h: HelpHandlerException => h.printHelpMessage }
      .fold(
        {
          case CLA.ErrorParser(code)      => code
          case ErrorProcessing(errorCode) => errorCode
          case _                          => 1
        },
        _ => 0
      )
  }

  // $COVERAGE-OFF$ Bootstrap to `Unit`
  final def main(args: Array[String]): Unit = {
    val runtime = makeRuntime
    val exitCode = runtime.unsafeRun(run(args.toList))
    println(s"ExitCode: $exitCode")
  }
  // $COVERAGE-ON$
}

object ZparkioApp {
  type ZPEnv[C <: CLA.Service] =
    zio.ZEnv with CLA[C] with Logger with SparkModule
}
