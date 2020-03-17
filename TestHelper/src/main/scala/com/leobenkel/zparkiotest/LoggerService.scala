package com.leobenkel.zparkiotest

import com.leobenkel.zparkio.Services.Logger
import zio.console.Console
import zio.{UIO, ZIO}

trait LoggerService extends Logger.Service {
  override def info(txt: => String): ZIO[Console, Throwable, Unit] =
    UIO(println(s"INFO: $txt"))
  override def error(txt: => String): ZIO[Console, Throwable, Unit] =
    UIO(println(s"ERROR: $txt"))
  override def debug(txt: => String): ZIO[Console, Throwable, Unit] =
    UIO(println(s"DEBUG: $txt"))
}
