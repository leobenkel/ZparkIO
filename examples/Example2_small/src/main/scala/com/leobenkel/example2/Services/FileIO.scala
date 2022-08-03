package com.leobenkel.example2.Services

import zio.{ZIO, ZLayer}

import scala.io.Source

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
      val content = file.getLines().toArray
      file.close()
      content
    }
  }

  val Live: ZLayer[Any, Throwable, FileIO] = ZLayer.succeed(new LiveService {})

  def apply(path: String): ZIO[FileIO, Throwable, Seq[String]] =
    ZIO.serviceWithZIO[FileIO](_.getFileContent(path))
}
