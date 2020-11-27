package com.leobenkel.zparkio.config.scallop

import com.leobenkel.zparkio.Env._
import org.rogach.scallop.{ArgType, DefaultConverters, ValueConverter}

object EnvironmentConvertor {
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
