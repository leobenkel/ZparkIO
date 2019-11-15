package com.leobenkel.zparkio.Services

import zio.ZIO
import zio.console.Console

trait Logger {
  def log: Logger.Service
}

object Logger {
  trait Service {
    def info(txt:  String): ZIO[Any with Console, Nothing, Unit]
    def error(txt: String): ZIO[Any with Console, Nothing, Unit]
    def debug(txt: String): ZIO[Any with Console, Nothing, Unit]
  }

  def info(txt: String): ZIO[Any with Console with Logger, Nothing, Unit] = {
    ZIO.environment[Logger].flatMap(_.log.info(txt))
  }

  def error(txt: String): ZIO[Any with Console with Logger, Nothing, Unit] = {
    ZIO.environment[Logger].flatMap(_.log.error(txt))
  }

  def debug(txt: String): ZIO[Any with Console with Logger, Nothing, Unit] = {
    ZIO.environment[Logger].flatMap(_.log.debug(txt))
  }
}
