package com.leobenkel.zparkioProfileExampleMoreComplex

import com.leobenkel.zparkio.Services.CommandLineArguments
import org.rogach.scallop.{ScallopConf, ScallopOption}
import zio.ZIO

case class Arguments(input: List[String])
    extends ScallopConf(input) with CommandLineArguments.Service {

  val databaseUsername: ScallopOption[String] = opt[String](
    default = Some("admin"),
    required = false,
    noshort = true
  )

  /*
   * Keep in mind that secrets should be provided through a Vault or env variable.
   * Do not pass password as command line argument please !
   */
  val databasePassword: ScallopOption[String] = opt[String](
    default = Some("123456"),
    required = false,
    noshort = true
  )

  val databaseHost: ScallopOption[String] = opt[String](
    default = Some("database://host.com/database"),
    required = false,
    noshort = true
  )

  val generatedInputSize: ScallopOption[Int] = opt[Int](
    default = Some(100),
    required = false,
    noshort = true,
    descr = "The size of the sample data generated"
  )

  val sparkConfig: ScallopOption[String] = opt[String](
    default = Some("foo"),
    required = false,
    noshort = true
  )
}

object Arguments {
  def apply[A](f: Arguments => A): ZIO[CommandLineArguments[Arguments], Throwable, A] = {
    CommandLineArguments.get[Arguments](f)
  }
}
