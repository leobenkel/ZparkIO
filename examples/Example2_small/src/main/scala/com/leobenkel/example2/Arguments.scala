package com.leobenkel.example2

import com.leobenkel.zparkio.Services.CommandLineArguments
import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import com.leobenkel.example2.Services.Database
import com.leobenkel.zparkio.config.scallop.CommandLineArgumentScallop
import org.rogach.scallop.{ScallopConf, ScallopOption}
import zio.ZIO

case class Arguments(input: List[String])
    extends ScallopConf(input) with CommandLineArgumentScallop.Service[Arguments] {

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

  lazy val credentials: Database.Credentials = Database.Credentials(
    user = databaseUsername(),
    psw = databasePassword(),
    host = databaseHost()
  )
}

object Arguments {
  def apply[A](f: Arguments => A): ZIO[CommandLineArguments[Arguments], Throwable, A] = {
    CommandLineArguments.get[Arguments].apply(f)
  }
}
