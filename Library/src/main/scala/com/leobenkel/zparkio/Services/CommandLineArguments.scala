package com.leobenkel.zparkio.Services

import com.leobenkel.zparkio.Env._
import org.rogach.scallop.exceptions.Help
import org.rogach.scallop.{Scallop, ScallopConf, ScallopOption}
import zio.console.Console
import zio.{Task, ZIO}

import scala.util.Try

trait CommandLineArguments[C <: CommandLineArguments.Service] {
  def cli: C
}

object CommandLineArguments {
  trait Builder[C <: CommandLineArguments.Service] {
    protected def createCli(args: List[String]): C
    def createCliSafely(args:     List[String]): ZIO[Any, Throwable, C] = {
      createCli(args).verifyInternal()
    }
  }

  trait Service extends ScallopConf {
    this.appendDefaultToDescription = true

    final override def verify(): Unit =
      throw new Exception(s"Do not call verify yourself! Zparkio calls it for you.")

    private[zparkio] def verifyInternal(): Task[this.type] = {
      ZIO.fromTry(Try {
        if (!verified) super.verify()
        this
      })
    }

    lazy final private[zparkio] val commandsDebug: Seq[String] =
      filteredSummary(Set.empty).split('\n').toSeq

    final val env: ScallopOption[Environment] = opt[Environment](
      required = true,
      noshort = true,
      default = Some(Environment.Local),
      descr = "Set the environment for the run."
    )

    lazy final protected val getAllMetrics: Seq[(String, Any)] = {
      builder.opts.map(o => (o.name, builder.get(o.name).getOrElse("<NONE>")))
    }

    final override def onError(e: Throwable): Unit = e match {
      case Help("") =>
        throw HelpHandlerException(builder, None)
      case Help(subCommand) =>
        throw HelpHandlerException(builder.findSubbuilder(subCommand).get, Some(subCommand))
      case other => throw other
    }
  }

  private[zparkio] case class HelpHandlerException(
    s:          Scallop,
    subCommand: Option[String]
  ) extends Throwable {
    private def print(msg: String): ZIO[Console, Throwable, Unit] = {
      ZIO.accessM[Console](_.console.putStrLn(msg))
    }

    lazy private val header: String = subCommand match {
      case None    => "Help:"
      case Some(s) => s"Help for '$s':"
    }

    def printHelpMessage: ZIO[zio.ZEnv, Throwable, Unit] = {
      for {
        _ <- print(header)
        _ <- ZIO.foreach(s.vers)(print)
        _ <- ZIO.foreach(s.bann)(print)
        _ <- print(s.help)
        _ <- ZIO.foreach(s.foot)(print)
      } yield {
        ()
      }
    }
  }

  private[zparkio] object ErrorParser {
    def unapply(e: Throwable): Option[Int] = e match {
      case _: Help                 => Some(0)
      case _: HelpHandlerException => Some(0)
      case _ => None
    }
  }

  def apply[C <: CommandLineArguments.Service](): ZIO[CommandLineArguments[C], Throwable, C] = {
    ZIO.access[CommandLineArguments[C]](_.cli)
  }

  type ZIO_CONFIG_SERVICE[A <: CommandLineArguments.Service] =
    ZIO[CommandLineArguments[A], Throwable, YourConfigWrapper[A]]

  def get[C <: CommandLineArguments.Service]: ZIO_CONFIG_SERVICE[C] = {
    apply[C]()
      .flatMap(_.verifyInternal())
      .map(YourConfigWrapper[C])
  }

  implicit class Shortcut[C <: CommandLineArguments.Service](z: ZIO_CONFIG_SERVICE[C]) {
    def apply[A](f: C => A): ZIO[CommandLineArguments[C], Throwable, A] = z.map(_.apply(f))
  }

  case class YourConfigWrapper[C <: CommandLineArguments.Service](config: C) {
    def apply[A](f: C => A): A = f(config)
  }

  def displayCommandLines[C <: CommandLineArguments.Service](
  ): ZIO[CommandLineArguments[C] with Logger with Console, Throwable, Unit] = {
    for {
      conf <- apply[C]()
      _    <- Logger.info("--------------Command Lines--------------")
      _    <- ZIO.foreach(conf.commandsDebug)(s => Logger.info(s))
      _    <- Logger.info("-----------------------------------------")
      _    <- Logger.info("")
    } yield {}
  }
}
