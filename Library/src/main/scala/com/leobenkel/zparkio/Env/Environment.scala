package com.leobenkel.zparkio.Env

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

  private val ValidEnvs: Seq[Environment] =
    Seq(
      Local,
      Development,
      Staging,
      Production
    )

  def parseEnv(input: String): Option[Environment] =
    ValidEnvs.find(_.validStrings.contains(input.toLowerCase))
}
