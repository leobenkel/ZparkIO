package com.leobenkel.zparkio.Services

import org.scalatest.freespec.AnyFreeSpec
import zio.{Exit, FiberRefs, RuntimeFlags, Task, Unsafe, ZEnvironment, ZIO}

import scala.util.Try

class ModuleFailZIOTest extends AnyFreeSpec {
  "Module" ignore {
    trait Module {
      def service: Module.Service
    }
    object Module {
      trait Service {
        def foo(bar: String): String
      }

      def apply(b: String): ZIO[Module, Throwable, String] =
        ZIO.serviceWith[Module](_.service.foo(b))
    }

    case class ModuleServiceIpml(dead: Boolean) extends Module.Service {
      println(s"SERVICE IS CREATED !!!")
      if(dead) throw new RuntimeException("It failed !")
      override def foo(bar: String): String = {
        println(bar)
        bar
      }
    }

    object ModuleServiceBuilder {
      def create(dead: Boolean): Task[Module.Service] =
        ZIO.attempt(ModuleServiceIpml(dead))
    }

    case class ModuleIpml(s: Module.Service) extends Module {
      lazy final override val service: Module.Service = s
    }

    "Might fail" in {
      val runtime = zio.Runtime(
        ZEnvironment.empty,
        FiberRefs.empty,
        RuntimeFlags.default
      )

      def jobRun: ZIO[Module, Throwable, String] =
        for {
          a <- Module("bar")
          b <- Module("foo")
        } yield s"$a - $b"

      Try {
        Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe.run {
          for {
            s <- ModuleServiceBuilder.create(true)
            a <- jobRun.provideEnvironment(ZEnvironment(ModuleIpml(s)))
          } yield a
        } match {
          case a @ Exit.Success(value) =>
            println(s"Intern: $value")
            a
          case Exit.Failure(cause)     =>
            println(s"Failed inside with: $cause")
            fail(cause.prettyPrint)
        }
      }} match {
        case scala.util.Success(value)     => println(s"Outside: $value")
        case scala.util.Failure(exception) =>
          println(s"Failed outside with : $exception")
          fail(exception)
      }
    }
  }
}
