package com.leobenkel.zparkio.TestUtils
import com.leobenkel.zparkio.Services.Logger
import zio.console.Console
import zio.internal.{Platform, PlatformLive}
import zio.{Runtime, UIO, ZIO}

case class TestRuntime() extends Runtime[Logger] {
  override val Environment: Logger = TestEnvironment()
  val Platform:             Platform = PlatformLive.Default
}

case class TestEnvironment() extends Logger {
  lazy final override val log: Logger.Service = LoggerTest()
}

case class LoggerTest() extends Logger.Service {
  override def info(txt: => String): ZIO[Console, Throwable, Unit] =
    UIO(println(s"INFO: $txt"))
  override def error(txt: => String): ZIO[Console, Throwable, Unit] =
    UIO(println(s"ERROR: $txt"))
  override def debug(txt: => String): ZIO[Console, Throwable, Unit] =
    UIO(println(s"DEBUG: $txt"))
}
