package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.CommandLineArguments.Helper.HelpHandlerException
import com.leobenkel.zparkio.Services.Logger.Logger
import com.leobenkel.zparkio.Services.SparkModule.SparkModule
import com.leobenkel.zparkio.Services.{CommandLineArguments => CLA, _}
import com.leobenkel.zparkio.ZparkioApp.BaseEnv
import zio.duration.Duration
import zio.internal.Platform
import zio.{BootstrapRuntime, Has, Tag, Task, UIO, ZIO, ZLayer}

trait ZparkioApp[C <: CLA.Service, ENV <: Has[_], OUTPUT] {

  // Shortcut types
  final protected type COMPLETE_ENV = ENV with ZparkioApp.ZPEnv[C]
  final protected type ZPARKIO_ENV = ZparkioApp.ZPEnv[C]
  final protected type FACTORY_SPARK = SparkModule.Factory[C]
  final protected type FACTORY_LOG = Logger.Factory
  final protected type FACTORY_CLI = CLA.Factory[C]

  // Tag for user env
  implicit def tagC:   Tag[C]
  implicit def tagEnv: Tag[ENV]

  // Build ZPARKIO environment
  protected def sparkFactory:  FACTORY_SPARK
  protected def loggerFactory: FACTORY_LOG
  protected def cliFactory: FACTORY_CLI = CLA.Factory()
  protected def makeCli(args:      List[String]): C
  final private def buildEnv(args: C): ZLayer[zio.ZEnv, Throwable, BaseEnv[C]] = {
    loggerFactory.assembleLogger >+>
      cliFactory.assembleCliBuilder(args) >+>
      sparkFactory.assembleSparkModule
  }

  // Build user environment
  protected def env: ZLayer[ZPARKIO_ENV, Throwable, ENV]

  // Core business logic
  protected def runApp(): ZIO[COMPLETE_ENV, Throwable, OUTPUT]

  // Default implementations
  protected def displayCommandLines: Boolean = true
  protected def processErrors(f: Throwable): Option[Int] = Some(1)
  protected def timedApplication:  Duration = Duration.Infinity
  protected def stopSparkAtTheEnd: Boolean = true

  // RUNTIME
  protected def makePlatform: Platform = {
    Platform.default
      .withReportFailure { cause =>
        if (cause.died) println(cause.prettyPrint)
      }
  }
  protected def makeRuntime: BootstrapRuntime = new BootstrapRuntime {
    override val platform: Platform = makePlatform
  }

  private object ErrorProcessing {
    def unapply(e: Throwable): Option[Int] = {
      processErrors(e)
    }
  }

  protected def app: ZIO[COMPLETE_ENV, Throwable, OUTPUT] = {
    for {
      _ <- if (displayCommandLines) CLA.displayCommandLines[C]() else UIO(())
      output <- runApp()
        .timeoutFail(ZparkioApplicationTimeoutException())(timedApplication)
      _ <- if (stopSparkAtTheEnd) {
        SparkModule().map { s =>
          s.sparkContext.stop()
          s.stop()
          ()
        }
      } else {
        Task(())
      }
    } yield {
      output
    }
  }

  protected def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    Task(makeCli(args))
      .map(buildEnv)
      .flatMap { baseEnv =>
        app
          .provideSomeLayer[zio.ZEnv with BaseEnv[C]](env)
          .provideSomeLayer[zio.ZEnv](baseEnv)
      }
      .catchSome { case h: HelpHandlerException => h.printHelpMessage }
      .fold(
        {
          case CLA.Helper.ErrorParser(code) => code
          case ErrorProcessing(errorCode)   => errorCode
          case _                            => 1
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
  type BaseEnv[C <: CLA.Service] = CLA.CommandLineArguments[C] with Logger with SparkModule
  type ZPEnv[C <: CLA.Service] = zio.ZEnv with BaseEnv[C]
}
