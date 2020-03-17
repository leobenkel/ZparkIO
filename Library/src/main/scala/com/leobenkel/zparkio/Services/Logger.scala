package com.leobenkel.zparkio.Services

import zio.console.Console
import zio.{Task, ZIO}

trait Logger {
  def log: Logger.Service
}

object Logger {
  trait Service {
    def info(txt:  => String): ZIO[Console, Throwable, Unit]
    def error(txt: => String): ZIO[Console, Throwable, Unit]
    def debug(txt: => String): ZIO[Console, Throwable, Unit]
  }

  def displayAllErrors(ex: Throwable): ZIO[Any with Console with Logger, Throwable, Unit] = {
    for {
      _ <- Logger.error(s"!!! Error: ${ex.toString}:")
      _ <- ZIO.foreach(ex.getStackTrace)(st => Logger.error(s"  -   $st"))
      _ <- Option(ex.getCause)
        .fold[ZIO[Any with Console with Logger, Throwable, Unit]](Task(()))(displayAllErrors)
    } yield {
      ()
    }
  }

  def info(txt: => String): ZIO[Console with Logger, Throwable, Unit] = {
    ZIO.accessM[Console with Logger](_.log.info(txt))
  }

  def error(txt: => String): ZIO[Console with Logger, Throwable, Unit] = {
    ZIO.accessM[Console with Logger](_.log.error(txt))
  }

  def debug(txt: => String): ZIO[Console with Logger, Throwable, Unit] = {
    ZIO.accessM[Console with Logger](_.log.debug(txt))
  }
}
