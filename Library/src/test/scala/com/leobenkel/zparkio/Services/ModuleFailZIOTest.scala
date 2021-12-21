package com.leobenkel.zparkio.Services

import org.scalatest.freespec.AnyFreeSpec
import scala.util.Try
import zio.{BootstrapRuntime, Exit, Task, ZIO}

class ModuleFailZIOTest extends AnyFreeSpec {
  "Module" ignore {
    trait Module {
      def service: Module.Service
    }
    object Module {
      trait Service {
        def foo(bar: String): String
      }

      def apply(b: String): ZIO[Module, Throwable, String] = ZIO.access[Module](_.service.foo(b))
    }

    case class ModuleServiceIpml(dead: Boolean) extends Module.Service {
      println(s"SERVICE IS CREATED !!!")
      if (dead)
        throw new RuntimeException("It failed !")
      override def foo(bar: String): String = {
        println(bar)
        bar
      }
    }

    object ModuleServiceBuilder {
      def create(dead: Boolean): Task[Module.Service] = Task(ModuleServiceIpml(dead))
    }

    case class ModuleIpml(s: Module.Service) extends Module {
      lazy final override val service: Module.Service = s
    }

    "Might fail" in {
      val runtime = new BootstrapRuntime {}

      def jobRun: ZIO[Module, Throwable, String] =
        (for {
          a <- Module("bar")
          b <- Module("foo")
        } yield s"$a - $b")

      Try {
        runtime.unsafeRunSync {
          for {
            s <- ModuleServiceBuilder.create(true)
            a <- jobRun.provide(ModuleIpml(s))
          } yield a
        } match {
          case a @ Exit.Success(value) =>
            println(s"Intern: $value")
            a
          case Exit.Failure(cause) =>
            println(s"Failed inside with: $cause")
            fail(cause.prettyPrint)
        }
      } match {
        case scala.util.Success(value) => println(s"Outside: $value")
        case scala.util.Failure(exception) =>
          println(s"Failed outside with : $exception")
          fail(exception)
      }
    }
  }
}
