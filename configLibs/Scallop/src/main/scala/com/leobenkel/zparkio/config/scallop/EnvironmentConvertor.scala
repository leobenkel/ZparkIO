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
      private object Extract {
        def unapply(input: (String, List[String])): Option[String] =
          input match {
            case (_, i :: Nil) => Some(i)
            case _             => None
          }
      }

      def parse(
          s: List[(String, List[String])]
      ): Either[String, Option[EnvironmentConverter.ArgType]] =
        s match {
          case Extract(i) :: Nil => Environment.parseEnv(i) match {
              case e @ Some(_) => Right(e)
              case None        => Left(s"Cannot find valid environment for input: '$i'")
            }
          case Nil               => Right(None)
          case _                 => Left("you should provide exactly one argument for this option")
        }

      val argType: ArgType.V = ArgType.SINGLE
    }
}
