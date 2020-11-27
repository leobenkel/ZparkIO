package com.leobenkel.example2.Services

import com.leobenkel.zparkio.Services.CommandLineArguments.CommandLineArguments
import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkio.implicits._
import com.leobenkel.example2.Arguments
import org.apache.spark.sql._
import zio.{Has, Task, ZIO, ZLayer}

object Database {
  type Database = Has[Service]

  case class Credentials(
    user: String,
    psw:  String,
    host: String
  )

  trait Service {
    final def query[A: Encoder](q: String): ZDS[A] = {
      for {
        s           <- SparkModule()
        queryResult <- Task(query(s, q))
      } yield {
        queryResult
      }
    }

    protected def query[A: Encoder](
      spark: SparkSession,
      query: String
    ): Dataset[A]
  }

  case class LiveService(credentials: Credentials) extends Database.Service {
    override protected def query[A: Encoder](
      spark: SparkSession,
      query: String
    ): Dataset[A] = {
      import spark.implicits._

      /** This is where:
        * {{{
        *   spark.read.option("","").load()
        * }}}
        * would go.
        */
      Seq[A]().toDS
    }
  }

  val Live: ZLayer[CommandLineArguments[Arguments], Throwable, Database] =
    ZLayer.fromService(args => LiveService(args.credentials))

  def apply[A: Encoder](query: String): ZDS_R[Database, A] = {
    ZIO.environment[Database].flatMap(_.get.query(query))
  }
}
