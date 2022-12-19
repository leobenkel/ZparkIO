package com.leobenkel.example2.Services

import scala.io.Source
import zio.{ZIO, ZLayer}

object FileIO {
  type FileIO = Service

  trait Service {
    protected def readFileContent(path: String): Seq[String]

    final def getFileContent(path: String): ZIO[Any, Throwable, Seq[String]] =
      ZIO.attempt(readFileContent(path))
  }

  trait LiveService extends FileIO.Service {
    override protected def readFileContent(path: String): Seq[String] = {
      val file    = Source.fromFile(path)
      val content = file.getLines().toSeq
      file.close()
      content
    }
  }

  val Live: ZLayer[Any, Throwable, FileIO] = ZLayer.succeed(new LiveService {})

  def apply(path: String): ZIO[FileIO, Throwable, Seq[String]] =
    ZIO.serviceWithZIO[FileIO](_.getFileContent(path))
}
