package com.leobenkel.zparkioProjectExample

import com.leobenkel.zparkio.Services.CommandLineArguments
import org.rogach.scallop.{ScallopConf, ScallopOption}
import zio.ZIO

case class Arguments(input: List[String])
    extends ScallopConf(input) with CommandLineArguments.Service {
  val inputId: ScallopOption[Int] = opt[Int](
    default = Some(10),
    required = false,
    noshort = true
  )

  val sparkFoo: ScallopOption[String] = opt[String](
    default = Some("hello"),
    required = false,
    noshort = true
  )
}

object Arguments {
  def apply[A](f: Arguments => A): ZIO[CommandLineArguments[Arguments], Throwable, A] = {
    CommandLineArguments.get[Arguments](f)
  }
}
