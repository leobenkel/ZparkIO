package com.leobenkel.zparkio.Services

import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import org.rogach.scallop.exceptions.{RequiredOptionNotFound, UnknownOption}
import org.rogach.scallop.{ScallopConf, ScallopOption}
import org.scalatest._
import zio.Exit.{Failure, Success}
import zio.{BootstrapRuntime, Layer, Task, ZIO, ZLayer}

class CommandLineArgumentsTest extends FreeSpec {
  "CommandLineService" - {
    case class ArgumentsService(input: Seq[String])
        extends ScallopConf(input) with CommandLineArguments.Service {
      val test: ScallopOption[String] = opt[String](
        default = None,
        required = true,
        noshort = true
      )
    }

    object Arguments {
      def get[A](
        f: ArgumentsService => A
      ): ZIO[CommandLineArguments[ArgumentsService], Throwable, A] = {
        CommandLineArguments.get[ArgumentsService].apply(f)
      }

      def apply(input: Seq[String]): Layer[Nothing, CommandLineArguments[ArgumentsService]] = {
        ZLayer.succeed(ArgumentsService(input))
      }
    }

    val runtime = new BootstrapRuntime {}

    "should work" in {
      val test: String = "qwe-asd-asd-zxc"

      runtime.unsafeRunSync {
        Arguments.get(_.test.toOption).provideLayer(Arguments(Seq("--test", test)))
      } match {
        case Success(Some(value)) => assertResult(value)(test)
        case Success(None)        => fail("Did not found any value")
        case Failure(ex)          => fail(ex.prettyPrint)
      }
    }

    "should fail - missing required" in {
      runtime.unsafeRunSync(for {
        arg <- Task(Arguments(Nil))
        a   <- Arguments.get(_.test.toOption).provideLayer(arg)
      } yield {
        a
      }) match {
        case Success(_)  => fail("Should have failed")
        case Failure(ex) => assertThrows[RequiredOptionNotFound](throw ex.squash)
      }
    }

    "should fail - unknonw option" in {
      runtime.unsafeRunSync(for {
        arg <- Task(Arguments(Seq("--abc", "foo")))
        a   <- Arguments.get(_.test.toOption).provideLayer(arg)
      } yield {
        a
      }) match {
        case Success(_)  => fail("Should have failed")
        case Failure(ex) => assertThrows[UnknownOption](throw ex.squash)
      }
    }
  }
}
