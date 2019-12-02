package com.leobenkel.zparkioProfileExampleMoreComplex.Services

import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkio.implicits._
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}
import zio.{Task, ZIO}

trait Database {
  def database: Database.Service
}

object Database {
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

  private trait LiveService extends Database.Service {

    override protected def query[A: Encoder](
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
      Seq[A]().toDS
    }

    protected def getCredentials: Credentials
  }

  trait Live extends Database {
    protected def getDatabaseCredentials: Credentials

    override def database: Database.Service = new Database.LiveService {
      override protected def getCredentials: Credentials = getDatabaseCredentials
    }
  }

  def apply[A: Encoder](query: String): ZDS_R[Database, A] = {
    ZIO.environment[Database].flatMap(_.database.query(query))
  }
}
