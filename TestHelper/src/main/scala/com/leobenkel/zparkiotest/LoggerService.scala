package com.leobenkel.zparkiotest

import com.leobenkel.zparkio.Services.Logger
import zio.{UIO, ZIO}
import zio.console.Console

trait LoggerService extends Logger.Service {
  override def info(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"INFO: $txt"))
  override def error(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"ERROR: $txt"))
  override def debug(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"DEBUG: $txt"))
}
