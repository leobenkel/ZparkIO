package com.leobenkel.zparkio.Services

import zio.{Has, ZIO}

object ZEnv {
  def apply[B, C, R <: Has[B], E](f: B => ZIO[R, E, C]): ZIO[R, E, C] = {
    ZIO.accessM[R](h => f(h.get))
  }
}
