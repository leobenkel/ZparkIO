package com.leobenkel.zparkiotest

import com.leobenkel.zparkio.Services.Logger
import zio.{Task, UIO}

trait LoggerService extends Logger.Service {
  override def info(txt: => String):  Task[Unit] = UIO(println(s"INFO: $txt"))
  override def error(txt: => String): Task[Unit] = UIO(println(s"ERROR: $txt"))
  override def debug(txt: => String): Task[Unit] = UIO(println(s"DEBUG: $txt"))
}
