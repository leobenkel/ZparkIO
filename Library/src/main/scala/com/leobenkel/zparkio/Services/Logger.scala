package com.leobenkel.zparkio.Services

import zio.console.Console
import zio.{Has, Task, ZIO, ZLayer}

object Logger {
  type Logger = Has[Logger.Service]

  trait Service {
    def info(txt:  => String): Task[Unit]
    def error(txt: => String): Task[Unit]
    def debug(txt: => String): Task[Unit]
  }

  trait Factory {
    protected def makeLogger(
      console: zio.console.Console.Service
    ): ZIO[Any, Throwable, Logger.Service]

    private[zparkio] def assembleLogger: ZLayer[Console, Throwable, Logger] =
      ZLayer.fromServiceM(makeLogger)
  }

  object Factory {
    def apply(make: Console.Service => Service): Factory = new Factory {
      override protected def makeLogger(console: Console.Service): ZIO[Any, Throwable, Service] = {
        Task(make(console))
      }
    }
  }

  def displayAllErrors(ex: Throwable): ZIO[Logger, Throwable, Unit] = {
    for {
      _ <- Logger.error(s"!!! Error: ${ex.toString}:")
      _ <- ZIO.foreach(ex.getStackTrace.toList)(st => Logger.error(s"  -   $st"))
      _ <- Option(ex.getCause)
        .fold[ZIO[Logger, Throwable, Unit]](Task(()))(displayAllErrors)
    } yield {
      ()
    }
  }

  val Live: ZLayer[Console, Throwable, Logger] = ZLayer.fromService { console =>
    new Logger.Service {
      override def info(txt:  => String): Task[Unit] = console.putStrLn(s"[INFO] $txt")
      override def error(txt: => String): Task[Unit] = console.putStrLn(s"[ERROR] $txt")
      override def debug(txt: => String): Task[Unit] = console.putStrLn(s"[DEBUG] $txt")
    }
  }

  def info(txt: => String): ZIO[Logger, Throwable, Unit] = ZIO.accessM[Logger](_.get.info(txt))

  def error(txt: => String): ZIO[Logger, Throwable, Unit] = ZIO.accessM[Logger](_.get.error(txt))

  def debug(txt: => String): ZIO[Logger, Throwable, Unit] = ZIO.accessM[Logger](_.get.debug(txt))
}
