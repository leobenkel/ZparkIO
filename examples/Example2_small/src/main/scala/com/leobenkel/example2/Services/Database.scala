package com.leobenkel.example2.Services

import com.leobenkel.example2.Arguments
import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkio.implicits._
import org.apache.spark.sql._
import zio.{ZIO, ZLayer}

object Database {
  type Database = Service

  case class Credentials(
      user: String,
      psw:  String,
      host: String
  )

  trait Service {
    final def query[A : Encoder](q: String): ZDS[A] =
      for {
        s           <- SparkModule()
        queryResult <- ZIO.attempt(query(s, q))
      } yield queryResult

    protected def query[A : Encoder](
        spark: SparkSession,
        query: String
    ): Dataset[A]
  }

  case class LiveService(credentials: Credentials) extends Database.Service {
    override protected def query[A : Encoder](
        spark: SparkSession,
        query: String
    ): Dataset[A] = {
      import spark.implicits._

      /**
       * This is where:
       * {{{
       *   spark.read.option("","").load()
       * }}}
       * would go.
       */
      Seq[A]().toDS()
    }
  }

  val Live: ZLayer[CommandLineArguments[Arguments], Throwable, Database] =
    ZLayer.fromZIO(ZIO.serviceWith[Arguments](args => LiveService(args.credentials)))

  def apply[A : Encoder](query: String): ZDS_R[Database, A] =
    ZIO.environment[Database].flatMap(_.get.query(query))
}
