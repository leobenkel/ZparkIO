package com.leobenkel.zparkioProjectExample

import com.leobenkel.zparkio.Services.{CommandLineArguments, Logger, SparkModule}
import zio.ZIO
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.system.System

case class RuntimeEnv(
  cliService:   CommandLineArguments.Service,
  sparkService: SparkModule.Service
) extends System.Live with Console.Live with Clock.Live with Random.Live with Blocking.Live
    with CommandLineArguments with Logger with FileIO.Live with SparkModule {

  lazy final override val cli:   CommandLineArguments.Service = cliService
  lazy final override val spark: SparkModule.Service = sparkService
  lazy final override val log: Logger.Service = new Logger.Service {
    override def info[E](txt:  String): ZIO[E, Nothing, Unit] = console.putStrLn(s"INFO: $txt")
    override def error[E](txt: String): ZIO[E, Nothing, Unit] = console.putStrLn(s"ERROR: $txt")
    override def debug[E](txt: String): ZIO[E, Nothing, Unit] = console.putStrLn(s"DEBUG: $txt")
  }

}
