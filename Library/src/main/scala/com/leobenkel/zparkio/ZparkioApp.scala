package com.leobenkel.zparkio

import com.leobenkel.zparkio.Services.CommandLineArguments.Helper.HelpHandlerException
import com.leobenkel.zparkio.Services.Logger.Logger
import com.leobenkel.zparkio.Services.SparkModule.SparkModule
import com.leobenkel.zparkio.Services.{CommandLineArguments => CLA, _}
import com.leobenkel.zparkio.ZparkioApp.BaseEnv
import zio.{Clock, Console, DefaultServices, Duration, FiberRefs, Random, Runtime, RuntimeFlags, System, ZIO, ZLayer}
import izumi.reflect.Tag

//scalastyle:off number.of.methods
trait ZparkioApp[C <: CLA.Service[C], ENV, OUTPUT] {

  // Shortcut types
  final protected type COMPLETE_ENV  = ENV with ZparkioApp.ZPEnv[C]
  final protected type ZPARKIO_ENV   = ZparkioApp.ZPEnv[C]
  final protected type FACTORY_SPARK = SparkModule.Factory[C]
  final protected type FACTORY_LOG   = Logger.Factory
  final protected type FACTORY_CLI   = CLA.Factory[C]
  final protected type ERROR_HANDLER = CLA.ConfigErrorParser

  // Tag for user env
  implicit def tagC:   Tag[C]
  implicit def tagEnv: Tag[ENV]
  implicit def zioT: zio.Tag[C]
  // Build ZPARKIO environment
  protected def sparkFactory:          FACTORY_SPARK
  protected def loggerFactory:         FACTORY_LOG
  protected def cliFactory:            FACTORY_CLI
  protected def makeConfigErrorParser: ERROR_HANDLER
  protected def makeCli(args: List[String]): C

  final protected def buildEnv(
      args: C
  ): ZLayer[Clock with Console with System with Random, Throwable, BaseEnv[C]] =
    loggerFactory.assembleLogger >+> cliFactory.assembleCliBuilder(args) >+>
      sparkFactory.assembleSparkModule

  // Build user environment
  protected def env: ZLayer[ZPARKIO_ENV, Throwable, ENV]

  // Core business logic
  protected def runApp(): ZIO[COMPLETE_ENV, Throwable, OUTPUT]

  // Default implementations
  protected def displayCommandLines: Boolean = true

  protected def processErrors(f: Throwable): Option[Int] = {
    // to silence warning about being unused
    locally(f)
    Some(1)
  }

  protected def timedApplication:  Duration = Duration.Infinity
  protected def stopSparkAtTheEnd: Boolean  = true

  protected def makeRuntime: Runtime[Clock with Console with System with Random] =
    zio.Runtime(
      DefaultServices.live,
      FiberRefs.empty,
      RuntimeFlags.default
    )

  private object ErrorProcessing {
    def unapply(e: Throwable): Option[Int] = processErrors(e)
  }

  protected def app: ZIO[COMPLETE_ENV, Throwable, OUTPUT] =
    for {
      _      <- if(displayCommandLines) CLA.displayCommandLines[C]() else ZIO.succeed(())
      output <- runApp().timeoutFail(ZparkioApplicationTimeoutException())(timedApplication)
      _      <-
        if(stopSparkAtTheEnd) SparkModule().map { s =>
          s.sparkContext.stop()
          s.stop()
          ()
        }
        else ZIO.attempt(())
    } yield output

  private def handleErrors(e: Throwable): Int = {
    val errorParser = makeConfigErrorParser.ErrorParser
    e match {
      case errorParser(code)          => code
      case ErrorProcessing(errorCode) => errorCode
      case _                          => 1
    }
  }

  protected def run(args: List[String]): ZIO[Clock with Console with System with Random , Nothing, Int] =
    ZIO.attempt(makeCli(args))
      .map(buildEnv)
      .flatMap { baseEnv =>
        app.provideSomeLayer[Clock with Console with System with Random with BaseEnv[C]](env)
          .provideSomeLayer(baseEnv)
      }
      .catchSome { case h: HelpHandlerException => h.printHelpMessage }
      .fold(handleErrors, _ => 0)

  // $COVERAGE-OFF$ Bootstrap to `Unit`
  final def main(args: Array[String]): Unit = {
    val runtime  = makeRuntime
    val exitCode = runtime.run(run(args.toList))
    println(s"ExitCode: $exitCode")
  }
  // $COVERAGE-ON$
}
//scalastyle:on number.of.methods

object ZparkioApp {
  type BaseEnv[C <: CLA.Service[C]] = CLA.CommandLineArguments[C] with Logger with SparkModule
  type ZPEnv[C <: CLA.Service[C]]   = Clock with Console with System with Random with BaseEnv[C]
}
