package com.leobenkel.zparkio.config.scallop

import com.leobenkel.zparkio.Services.CommandLineArguments
import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import com.leobenkel.zparkio.config.scallop.CommandLineArgumentScallop.HelpHandlerException
import org.rogach.scallop.{ScallopConf, ScallopOption, Subcommand}
import org.rogach.scallop.exceptions.{RequiredOptionNotFound, UnknownOption}
import org.scalatest.freespec.AnyFreeSpec
import zio.{BootstrapRuntime, Layer, Task, ZIO, ZLayer}
import zio.Exit.{Failure, Success}

class CommandLineArgumentScallopTest extends AnyFreeSpec {
  "CommandLineService" - {
    case class ArgumentsService(input: Seq[String])
        extends ScallopConf(input)
        with CommandLineArgumentScallop.Service[ArgumentsService] {
      val test: ScallopOption[String] =
        opt[String](
          default = None,
          required = true,
          noshort = true
        )
    }

    object Arguments {
      def get[A](
          f: ArgumentsService => A
      ): ZIO[CommandLineArguments[ArgumentsService], Throwable, A] =
        CommandLineArguments.get[ArgumentsService].apply(f)

      def apply(input: Seq[String]): Layer[Nothing, CommandLineArguments[ArgumentsService]] =
        ZLayer.succeed(ArgumentsService(input))
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
      } yield a) match {
        case Success(_)  => fail("Should have failed")
        case Failure(ex) => assertThrows[RequiredOptionNotFound](throw ex.squash)
      }
    }

    "should fail - unknonw option" in {
      runtime.unsafeRunSync(for {
        arg <- Task(Arguments(Seq("--abc", "foo")))
        a   <- Arguments.get(_.test.toOption).provideLayer(arg)
      } yield a) match {
        case Success(_)  => fail("Should have failed")
        case Failure(ex) => assertThrows[UnknownOption](throw ex.squash)
      }
    }

    "help should look good" in {
      class Argument(args: List[String])
          extends ScallopConf(args)
          with CommandLineArgumentScallop.Service[Argument] {
        val foo: ScallopOption[Int] =
          opt[Int](
            descr = "Test",
            default = Some(87)
          )

        val sub =
          new Subcommand("test_sub") {
            val a: ScallopOption[Boolean] =
              opt[Boolean](
                descr = "Test",
                default = Some(false)
              )

            val b: ScallopOption[Int] =
              opt[Int](
                descr = "Test",
                default = Some(34)
              )
          }
        addSubcommand(sub)
      }
      val arg = new Argument(List("--help"))
      runtime.unsafeRunSync(
        arg.checkValidity().tapError { case h: HelpHandlerException => h.printHelpMessage }
      ) match {
        case Success(a)  => assert(a.verified)
        case Failure(ex) => assertThrows[HelpHandlerException](throw ex.squash)
      }
    }
  }
}
