package com.leobenkel.example1

import com.leobenkel.zparkio.Services._
import zio.{Console, Task}

case class Log(console: Console) extends Logger.Service {
  override def info(txt: => String): Task[Unit] = console.printLine(s"INFO: $txt")

  override def error(txt: => String): Task[Unit] = console.printLine(s"ERROR: $txt")

  override def debug(txt: => String): Task[Unit] = console.printLine(s"DEBUG: $txt")
}
