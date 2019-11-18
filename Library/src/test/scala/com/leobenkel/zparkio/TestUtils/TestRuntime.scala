package com.leobenkel.zparkio.TestUtils
import com.leobenkel.zparkio.Services.Logger
import zio.console.Console
import zio.internal.{Platform, PlatformLive}
import zio.{Runtime, UIO, ZIO}

case class TestRuntime() extends Runtime[Any with Logger] {
  override val Environment: Any with Logger = TestEnvironment()
  val Platform:             Platform = PlatformLive.Default
}

case class TestEnvironment() extends Logger {
  override def log: Logger.Service = LoggerTest()
}

case class LoggerTest() extends Logger.Service {
  override def info(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"INFO: $txt"))
  override def error(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"ERROR: $txt"))
  override def debug(txt: String): ZIO[Any with Console, Nothing, Unit] =
    UIO(println(s"DEBUG: $txt"))
}
