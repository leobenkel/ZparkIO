package com.leobenkel.zparkioProjectExample

import com.leobenkel.zparkio.Services._
import zio.Task
import zio.console.Console

case class Log(console: Console.Service) extends Logger.Service {
  override def info(txt: => String): Task[Unit] =
    console.putStrLn(s"INFO: $txt")

  override def error(txt: => String): Task[Unit] =
    console.putStrLn(s"ERROR: $txt")

  override def debug(txt: => String): Task[Unit] =
    console.putStrLn(s"DEBUG: $txt")
}
