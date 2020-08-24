package com.leobenkel.zparkio.Services

import org.scalatest.freespec.AnyFreeSpec
import zio.{BootstrapRuntime, Exit, ZIO}

import scala.util.Try

class ModuleFailTest extends AnyFreeSpec {
  "Module" ignore {
    trait Module {
      def service: Module.Service
    }
    object Module {
      trait Service {
        def foo(bar: String): String
      }

      def apply(b: String): ZIO[Module, Nothing, String] = {
        ZIO.access[Module](_.service.foo(b))
      }
    }

    case class ModuleServiceIpml(dead: Boolean) extends Module.Service {
      if (dead) {
        throw new RuntimeException("It failed !")
      }
      override def foo(bar: String): String = {
        println(bar)
        bar
      }
    }

    case class ModuleIpml(dead: Boolean) extends Module {
      lazy final override val service: Module.Service = ModuleServiceIpml(dead)
    }

    "Might fail" in {
      val runtime = new BootstrapRuntime {}

      Try {
        runtime.unsafeRunSync {
          (for {
            a <- Module("bar")
            b <- Module("foo")
          } yield { s"$a - $b" }).provide(ModuleIpml(false))
        } match {
          case a @ Exit.Success(value) =>
            println(s"Intern: $value")
            a
          case Exit.Failure(cause) => fail(cause.prettyPrint)
        }
      } match {
        case scala.util.Success(value)     => println(s"Outside: $value")
        case scala.util.Failure(exception) => fail(exception)
      }
    }
  }
}
