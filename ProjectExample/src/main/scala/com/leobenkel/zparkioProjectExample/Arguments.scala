package com.leobenkel.zparkioProjectExample

import com.leobenkel.zparkio.Services.CommandLineArguments
import org.rogach.scallop.{ScallopConf, ScallopOption}

case class Arguments(input: List[String])
    extends ScallopConf(input) with CommandLineArguments.Service {
  val inputId: ScallopOption[Int] = opt[Int](
    default = None,
    required = false,
    noshort = true
  )

  val sparkFoo: ScallopOption[String] = opt[String](
    default = Some("hello"),
    required = false,
    noshort = true
  )

  verify()
}
