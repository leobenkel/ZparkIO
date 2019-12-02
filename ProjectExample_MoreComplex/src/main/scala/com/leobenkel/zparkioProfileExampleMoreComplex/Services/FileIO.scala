package com.leobenkel.zparkioProfileExampleMoreComplex.Services

import zio.{Task, ZIO}

import scala.io.Source

trait FileIO {
  def fileIO: FileIO.Service
}

object FileIO {
  trait Service {
    protected def readFileContent(path: String): Seq[String]

    final def getFileContent(path: String): ZIO[Any, Throwable, Seq[String]] = {
      Task(readFileContent(path))
    }
  }

  private trait LiveService extends FileIO.Service {
    override protected def readFileContent(path: String): Seq[String] = {
      val file = Source.fromFile(path)
      val content = file.getLines().toArray
      file.close()
      content
    }
  }

  trait Live extends FileIO {
    override def fileIO: Service = new LiveService {}
  }

  def apply(path: String): ZIO[FileIO, Throwable, Seq[String]] = {
    ZIO.accessM[FileIO](_.fileIO.getFileContent(path))
  }
}
