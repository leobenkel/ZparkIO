package com.leobenkel.zparkio.config.scallop

import com.leobenkel.zparkio.Env._
import com.leobenkel.zparkio.Services.{CommandLineArguments, Logger}
import com.leobenkel.zparkio.Services.CommandLineArguments.ConfigErrorParser
import com.leobenkel.zparkio.Services.Logger.Logger
import org.rogach.scallop.{Scallop, ScallopConf, ScallopOption}
import org.rogach.scallop.exceptions.{Help, ScallopException}
import scala.util.Try
import zio.{console, Task, ZIO}
import zio.console.Console

object CommandLineArgumentScallop {
  trait Service[C <: CommandLineArguments.Service[C]]
      extends ScallopConf with CommandLineArguments.Service[C] { this: ScallopConf with C =>
    this.appendDefaultToDescription = true

    final override def verify(): Unit =
      throw new Exception(s"Do not call verify yourself! Zparkio calls it for you.")

    final override def checkValidity(): Task[C] =
      ZIO.fromTry(Try {
        if (!verified) super.verify()
        this
      })

    lazy final override val commandsDebug: Seq[String] = {
      val (active, inactive) = filteredSummary(Set.empty)
        .split('\n')
        .partition(_.trim.startsWith("*"))

      (active.sorted :+ "") ++ inactive.sorted
    }

    final val env: ScallopOption[Environment] = opt[Environment](
      required = true,
      noshort = true,
      default = Some(Environment.Local),
      descr = "Set the environment for the run."
    )(EnvironmentConverter.Parser)

    lazy final protected val getAllMetrics: Seq[(String, Any)] =
      builder.opts.map(o => (o.name, builder.get(o.name).getOrElse("<NONE>")))

    final override def onError(e: Throwable): Unit =
      e match {
        case Help("") =>
          throw HelpHandlerException(builder, None)
        case Help(subCommand) =>
          throw HelpHandlerException(builder.findSubbuilder(subCommand).get, Some(subCommand))
        case other => throw other
      }
  }

  case class HelpHandlerException(
    s:          Scallop,
    subCommand: Option[String]
  ) extends Throwable with CommandLineArguments.Helper.HelpHandlerException {
    private def print(msg: String): ZIO[Console, Throwable, Unit] = console.putStr(msg)

    lazy private val header: String = subCommand match {
      case None    => "Help:\n"
      case Some(s) => s"Help for '$s':\n"
    }

    final override def printHelpMessage: ZIO[zio.ZEnv, Throwable, Unit] =
      for {
        _ <- print(header)
        _ <- ZIO.foreach_(s.vers)(print)
        _ <- ZIO.foreach_(s.bann)(print)
        _ <- print(s.help)
        _ <- ZIO.foreach_(s.foot)(print)
        _ <- print("\n")
      } yield ()
  }

  object ErrorParser extends ConfigErrorParser {
    override def handleConfigParsingErrorCode(e: Throwable): Option[Int] =
      e match {
        case _: Help                 => Some(0)
        case _: HelpHandlerException => Some(0)
        case _ => None
      }
  }

  private[zparkio] trait Factory[C <: CommandLineArguments.Service[C]]
      extends CommandLineArguments.Factory[C] {
    final override protected def handleErrors(
      t: Throwable
    ): ZIO[Logger, Throwable, Unit] =
      t match {
        case cliError: ScallopException => Logger.displayAllErrors(cliError)
        case _ => ZIO.unit
      }
  }

  object Factory {
    def apply[C <: CommandLineArguments.Service[C]](): CommandLineArguments.Factory[C] =
      new Factory[C] {}
  }
}
