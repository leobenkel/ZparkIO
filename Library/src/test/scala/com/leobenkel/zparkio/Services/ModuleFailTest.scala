package com.leobenkel.zparkio.Services

import org.scalatest.freespec.AnyFreeSpec
import zio.{Exit, FiberRefs, RuntimeFlags, Unsafe, ZEnvironment, ZIO}

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

      def apply(b: String): ZIO[Module, Nothing, String] = ZIO.serviceWith[Module](_.service.foo(b))
    }

    case class ModuleServiceIpml(dead: Boolean) extends Module.Service {
      if(dead) throw new RuntimeException("It failed !")
      override def foo(bar: String): String = {
        println(bar)
        bar
      }
    }

    case class ModuleIpml(dead: Boolean) extends Module {
      lazy final override val service: Module.Service = ModuleServiceIpml(dead)
    }

    "Might fail" in {
      val runtime = zio.Runtime(
        ZEnvironment.empty,
        FiberRefs.empty,
        RuntimeFlags.default
      )

      Try {
        Unsafe.unsafe{ implicit unsafe =>
        runtime.unsafe.run {
          (for {
            a <- Module("bar")
            b <- Module("foo")
          } yield s"$a - $b")
            .provideEnvironment(ZEnvironment.apply(ModuleIpml(false)))
        }} match {
          case a @ Exit.Success(value) =>
            println(s"Intern: $value")
            a
          case Exit.Failure(cause)     => fail(cause.prettyPrint)
        }
      } match {
        case scala.util.Success(value)     => println(s"Outside: $value")
        case scala.util.Failure(exception) => fail(exception)
      }
    }
  }
}
