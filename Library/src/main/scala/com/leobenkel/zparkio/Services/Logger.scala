package com.leobenkel.zparkio.Services

import zio.ZIO

trait Logger {
  def log: Logger.Service
}

object Logger {
  trait Service {
    def info[E](txt:  String): ZIO[E, Nothing, Unit]
    def error[E](txt: String): ZIO[E, Nothing, Unit]
    def debug[E](txt: String): ZIO[E, Nothing, Unit]
  }

  def info[E <: Logger](txt: String): ZIO[E, Nothing, Unit] = {
    ZIO.environment[Logger].flatMap(_.log.info(txt))
  }

  def error[E <: Logger](txt: String): ZIO[E, Nothing, Unit] = {
    ZIO.environment[Logger].flatMap(_.log.error(txt))
  }

  def debug[E <: Logger](txt: String): ZIO[E, Nothing, Unit] = {
    ZIO.environment[Logger].flatMap(_.log.debug(txt))
  }
}
