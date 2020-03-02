package com.leobenkel.zparkio

import zio.ZIO

import scala.concurrent.{ExecutionContext, Future}

/**
  * Useful in transition period to convert Future to ZIO components.
  */
object ZFuture {
  implicit class ToZio[A](f: ExecutionContext => Future[A]) {
    def toZIO[R]: ZIO[R, Throwable, A] = ZIO.fromFuture(f)
  }

  implicit class ToZioNoF[A](f: ExecutionContext => A) {
    def toZIO[R]: ZIO[R, Throwable, A] = ZIO.fromFuture(ex => Future(f(ex))(ex))
  }

  implicit class ToZioNoEx[A](f: Future[A]) {
    def toZIO[R]: ZIO[R, Throwable, A] = ZIO.fromFuture(_ => f)
  }
}
