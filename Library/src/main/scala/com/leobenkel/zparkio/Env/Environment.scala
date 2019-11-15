package com.leobenkel.zparkio.Env

import org.rogach.scallop.{ArgType, DefaultConverters, ValueConverter}

sealed trait Environment {
  protected def getValidStrings: Seq[String]
  lazy final val validStrings: Seq[String] = getValidStrings.map(_.trim.toLowerCase)
}

object Environment {
  case object Local extends Environment {
    override protected def getValidStrings: Seq[String] = Seq("local")
  }
  case object Development extends Environment {
    override protected def getValidStrings: Seq[String] = Seq("dev", "development")
  }
  case object Staging extends Environment {
    override protected def getValidStrings: Seq[String] = Seq("staging", "stage", "stg")
  }
  case object Production extends Environment {
    override protected def getValidStrings: Seq[String] = Seq("production", "prod")
  }

  private val ValidEnvs: Seq[Environment] = Seq(
    Local,
    Development,
    Staging,
    Production
  )

  def parseEnv(input: String): Option[Environment] = {
    ValidEnvs.find(_.validStrings.contains(input.toLowerCase))
  }

  implicit val EnvironmentParser: ValueConverter[Environment] = EnvironmentConverter.Parser
}

object EnvironmentConverter extends DefaultConverters {
  type ArgType = Environment

  val Parser: ValueConverter[EnvironmentConverter.ArgType] =
    new ValueConverter[EnvironmentConverter.ArgType] {
      def parse(
        s: List[(String, List[String])]
      ): Either[String, Option[EnvironmentConverter.ArgType]] = {
        s match {
          case (_, i :: Nil) :: Nil =>
            Environment.parseEnv(i) match {
              case Some(e) => Right(Some(e))
              case None    => Left(s"Cannot find valid environment for input: '$i'")
            }
          case Nil => Right(None)
          case _   => Left("you should provide exactly one argument for this option")
        }
      }
      val argType: ArgType.V = ArgType.SINGLE
    }
}
