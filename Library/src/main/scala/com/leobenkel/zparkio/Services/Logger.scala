package com.leobenkel.zparkio.Services

import zio.ZIO
import zio.console.Console

trait Logger {
  def log: Logger.Service
}

object Logger {
  trait Service {
    def info(txt:  => String): ZIO[Console, Throwable, Unit]
    def error(txt: => String): ZIO[Console, Throwable, Unit]
    def debug(txt: => String): ZIO[Console, Throwable, Unit]
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
