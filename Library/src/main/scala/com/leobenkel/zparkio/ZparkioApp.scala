package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import com.leobenkel.zparkio.Services.CommandLineArguments.Helper.HelpHandlerException
import com.leobenkel.zparkio.Services.Logger.Logger
import com.leobenkel.zparkio.Services.SparkModule.SparkModule
import com.leobenkel.zparkio.Services.{CommandLineArguments => CLA, _}
import zio.duration.Duration
import zio.internal.Platform
import zio.{BootstrapRuntime, Has, Tag, Task, UIO, ZIO, ZLayer}

trait ZparkioApp[C <: CLA.Service, ENV <: Has[_], OUTPUT]
    extends Logger.Factory with CLA.Factory[C] with SparkModule.Factory[C] {

  implicit def tag1: Tag[C]
  implicit def tag2: Tag[ENV]

  def env: ZLayer[Any, Nothing, ENV]

  protected def makeSparkBuilder: SparkModule.Builder[C]
  protected def displayCommandLines: Boolean = true

  protected def runApp(): ZIO[ENV with ZparkioApp.ZPEnv[C], Throwable, OUTPUT]

  protected def processErrors(f: Throwable): Option[Int] = Some(1)
  protected def timedApplication: Duration = Duration.Infinity

  protected def makePlatform: Platform = {
    Platform.default
      .withReportFailure { cause =>
        if (cause.died) println(cause.prettyPrint)
      }
  }

  def makeRuntime: BootstrapRuntime = new BootstrapRuntime {
    override val platform: Platform = makePlatform
  }

  object ErrorProcessing {
    def unapply(e: Throwable): Option[Int] = {
      processErrors(e)
    }
  }

  protected def makeEnv: ZLayer[ZparkioApp.ZPEnv[C], Throwable, ENV]

  protected def buildEnv(
    args: List[String]
  ): ZLayer[zio.ZEnv, Throwable, Logger with CommandLineArguments[C] with SparkModule] = {
    this.assembleLogger >+>
      this.assembleCliBuilder(args) >+>
      this.assembleSparkModule
  }

  protected def stopSparkAtTheEnd: Boolean = true

  protected def app(args: List[String]): ZIO[ENV with ZparkioApp.ZPEnv[C], Throwable, OUTPUT] = {
    for {
      _ <- if (displayCommandLines) CLA.displayCommandLines[C]() else UIO(())
      output <- runApp()
        .provideSomeLayer[ZparkioApp.ZPEnv[C]](makeEnv)
        // This line has an error because it wants `ENV with Clock`
        // but `ENV` already contains `Clock`.
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
    app(args)
    // this line has the following error:
    // Cannot prove that zio.ZEnv with Logger with CommandLineArguments[C] with SparkModule <:< ENV.
      .provideCustomLayer(env ++ buildEnv(args))
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
