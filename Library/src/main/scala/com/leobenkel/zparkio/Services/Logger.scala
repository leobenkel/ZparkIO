package com.leobenkel.zparkio.Services

import zio.{Console, Task, ZIO, ZLayer}

object Logger {
  type Logger = Logger.Service

  trait Service {
    def info(txt:  => String): Task[Unit]
    def error(txt: => String): Task[Unit]
    def debug(txt: => String): Task[Unit]
  }

  trait Factory {
    protected def makeLogger(
        console: zio.Console
    ): ZIO[Any, Throwable, Logger.Service]

    private[zparkio] def assembleLogger: ZLayer[Console, Throwable, Logger] =
      ZLayer.fromZIO(ZIO.serviceWithZIO[Console](console => makeLogger(console)))
  }

  object Factory {
    def apply(make: Console => Service): Factory =
      new Factory {
        override protected def makeLogger(
            console: Console
        ): ZIO[Any, Throwable, Service] = ZIO.attempt(make(console))
      }
  }

  def displayAllErrors(ex: Throwable): ZIO[Logger, Throwable, Unit] =
    for {
      _ <- Logger.error(s"!!! Error: ${ex.toString}:")
      _ <- ZIO.foreach(ex.getStackTrace.toList)(st => Logger.error(s"  -   $st"))
      _ <- Option(ex.getCause).fold[ZIO[Logger, Throwable, Unit]](ZIO.attempt(()))(displayAllErrors)
    } yield ()

  val Live: ZLayer[Console, Throwable, Logger] =
    ZLayer.fromZIO(ZIO.serviceWith[Console]{ console =>
      new Logger.Service {
        override def info(txt: => String):  Task[Unit] = console.printLine(s"[INFO] $txt")
        override def error(txt: => String): Task[Unit] = console.printLine(s"[ERROR] $txt")
        override def debug(txt: => String): Task[Unit] = console.printLine(s"[DEBUG] $txt")
      }
    })

  def info(txt: => String): ZIO[Logger, Throwable, Unit] = ZIO.serviceWithZIO[Logger](_.info(txt))

  def error(txt: => String): ZIO[Logger, Throwable, Unit] = ZIO.serviceWithZIO[Logger](_.error(txt))

  def debug(txt: => String): ZIO[Logger, Throwable, Unit] = ZIO.serviceWithZIO[Logger](_.debug(txt))
}
