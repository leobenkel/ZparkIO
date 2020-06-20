package com.leobenkel.zparkio.TestUtils
import com.leobenkel.zparkio.Services.Logger
import zio.{Task, UIO}

case class LoggerTest() extends Logger.Service {
  override def info(txt: => String): Task[Unit] =
    UIO(println(s"INFO: $txt"))
  override def error(txt: => String): Task[Unit] =
    UIO(println(s"ERROR: $txt"))
  override def debug(txt: => String): Task[Unit] =
    UIO(println(s"DEBUG: $txt"))
}
