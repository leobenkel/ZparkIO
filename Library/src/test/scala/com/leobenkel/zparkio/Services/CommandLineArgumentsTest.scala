package com.leobenkel.zparkio.Services

import com.leobenkel.zparkio.TestUtils.TestRuntime
import org.rogach.scallop.exceptions.{RequiredOptionNotFound, UnknownOption}
import org.rogach.scallop.{ScallopConf, ScallopOption}
import org.scalatest._
import zio.Exit.{Failure, Success}
import zio.{Task, ZIO}

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
      def apply[A](
        f: ArgumentsService => A
      ): ZIO[CommandLineArguments[ArgumentsService], Throwable, A] = {
        CommandLineArguments.get[ArgumentsService](f)
      }
    }

    case class Arguments(input: Seq[String]) extends CommandLineArguments[ArgumentsService] {
      override def cli: ArgumentsService = ArgumentsService(input)
    }

    val runtime = TestRuntime()

    "should work" in {
      val test: String = "qwe-asd-asd-zxc"

      runtime.unsafeRunSync {
        Arguments(_.test.toOption).provide(Arguments(Seq("--test", test)))
      } match {
        case Success(Some(value)) => assertResult(value)(test)
        case Success(None)        => fail("Did not found any value")
        case Failure(ex)          => fail(ex.prettyPrint)
      }
    }

    "should fail - missing required" in {
      runtime.unsafeRunSync(for {
        arg <- Task(Arguments(Nil))
        a   <- Arguments(_.test.toOption).provide(arg)
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
        a   <- Arguments(_.test.toOption).provide(arg)
      } yield {
        a
      }) match {
        case Success(_)  => fail("Should have failed")
        case Failure(ex) => assertThrows[UnknownOption](throw ex.squash)
      }
    }
  }
}
