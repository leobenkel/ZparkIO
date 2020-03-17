package com.leobenkel.zparkioProjectExample

import com.leobenkel.zparkio.Services._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.system.System
import zio.{ZIO, console}

case class RuntimeEnv(
  cliService:   Arguments,
  sparkService: SparkModule.Service
) extends System.Live with Console.Live with Clock.Live with Random.Live with Blocking.Live
    with CommandLineArguments[Arguments] with Logger with FileIO.Live with SparkModule {

  lazy final override val cli:   Arguments = cliService
  lazy final override val spark: SparkModule.Service = sparkService
  lazy final override val log:   Logger.Service = new Log()

}

class Log extends Logger.Service {
  override def info(txt: => String): ZIO[Console, Throwable, Unit] =
    console.putStrLn(s"INFO: $txt")

  override def error(txt: => String): ZIO[Console, Throwable, Unit] =
    console.putStrLn(s"ERROR: $txt")

  override def debug(txt: => String): ZIO[Console, Throwable, Unit] =
    console.putStrLn(s"DEBUG: $txt")
}
