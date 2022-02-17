package com.leobenkel.example2.Services

import scala.io.Source
import zio.{Has, Task, ZIO, ZLayer}

object FileIO {
  type FileIO = Has[Service]

  trait Service {
    protected def readFileContent(path: String): Seq[String]

    final def getFileContent(path: String): ZIO[Any, Throwable, Seq[String]] =
      Task(readFileContent(path))
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
    ZIO.accessM[FileIO](_.get.getFileContent(path))
}
