package com.leobenkel.zparkio.Services

import com.leobenkel.zparkio.Env._
import org.rogach.scallop.exceptions.Help
import org.rogach.scallop.{Scallop, ScallopConf, ScallopOption}
import zio.console.Console
import zio.{Task, ZIO}

trait CommandLineArguments {
  def cli: CommandLineArguments.Service
}

object CommandLineArguments {
  trait Builder[C <: CommandLineArguments.Service] {
    protected def createCli(args: List[String]): C
    def createCliSafely(args:     List[String]): ZIO[Any, Throwable, C] = Task(createCli(args))
  }

  trait Service extends ScallopConf {
    this.appendDefaultToDescription = true

    final val env: ScallopOption[Environment] = opt[Environment](
      required = true,
      noshort = true,
      default = Some(Environment.Local),
      descr = "Set the environment for the run."
    )

    final protected def getAllMetrics: Seq[(String, Any)] = {
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
    private def print(msg: String): ZIO[Console, Nothing, Unit] = {
      ZIO.accessM[Console](_.console.putStrLn(msg))
    }

    lazy private val header: String = subCommand match {
      case None    => "Help:"
      case Some(s) => s"Help for '$s':"
    }

    def printHelpMessage: ZIO[zio.ZEnv, Nothing, Unit] = {
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
      case h: Help                 => Some(0)
      case h: HelpHandlerException => Some(0)
      case _ => None
    }
  }

  def apply(): ZIO[Any with CommandLineArguments, Nothing, CommandLineArguments.Service] = {
    ZIO.access[CommandLineArguments](_.cli)
  }

  type ZIO_CONFIG_SERVICE[A <: CommandLineArguments.Service] =
    ZIO[Any with CommandLineArguments, Throwable, YourConfigWrapper[A]]

  def get[A <: CommandLineArguments.Service]: ZIO_CONFIG_SERVICE[A] = {
    apply().map(c => YourConfigWrapper(c.asInstanceOf[A]))
  }

  implicit class Shortcut[C <: CommandLineArguments.Service](z: ZIO_CONFIG_SERVICE[C]) {
    def apply[A](f: C => A): ZIO[CommandLineArguments, Throwable, A] = z.map(_.apply(f))
  }

  case class YourConfigWrapper[C <: CommandLineArguments.Service](config: C) {
    def apply[A](f: C => A): A = f(config)
  }

  val DisplayCommandLines: ZIO[Any with CommandLineArguments with Logger, Nothing, Unit] = {
    for {
      conf <- apply()
      _    <- Logger.info("----------------------------")
      _    <- ZIO.foreach(conf.filteredSummary(Set.empty).split('\n').toSeq)(s => Logger.info(s))
      _    <- Logger.info("----------------------------")
    } yield {}
  }
}
