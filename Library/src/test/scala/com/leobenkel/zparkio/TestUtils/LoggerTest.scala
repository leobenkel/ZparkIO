package com.leobenkel.zparkio.TestUtils

import com.leobenkel.zparkio.Services.Logger
import zio.{Task, ZIO}

case class LoggerTest() extends Logger.Service {
  override def info(txt:  => String): Task[Unit] = ZIO.succeed(println(s"INFO: $txt"))
  override def error(txt: => String): Task[Unit] = ZIO.succeed(println(s"ERROR: $txt"))
  override def debug(txt: => String): Task[Unit] = ZIO.succeed(println(s"DEBUG: $txt"))
}
