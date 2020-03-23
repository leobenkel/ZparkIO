package com.leobenkel.zparkioProjectExample

import com.leobenkel.zparkiotest.SparkExecution
import org.scalatest.FreeSpec

class RunTest extends FreeSpec {
  "Run" - {
    "Should run" ignore {
      val REBUILD_JAR = true
      import sys.process._

      val version = "cat VERSION".!!.replaceAll("v", "").replaceAll("\n", "")
      println(s"Version: '$version'")
      val rootPath = System.getProperty("user.dir")
      println(rootPath)
      val jarPath = s"$rootPath/ProjectExample/target/docker/0/zparkio_testProject-$version-all.jar"
      println(jarPath)

      if (REBUILD_JAR || s"ls $jarPath".! != 0) {
        val out: Int =
          ("pwd" #&&
            ("sbt" ::
              "; project projectExample" ::
              "; set test in assembly := {}" ::
              s"""; set version := "$version" """ ::
              "; docker" ::
              Nil))
            .!(new ProcessLogger {
              override def out(s: => String): Unit = System.out.println(s)

              override def err(s: => String): Unit = System.err.println(s)

              override def buffer[T](f: => T): T = f
            })

        println(out)
        assert(out == 0)
      }
      assert(s"ls $jarPath".! == 0)

      val s = SparkExecution(
        sparkConf = Map.empty,
        sparkFile = Map.empty,
        pathToJar = jarPath,
        mainClassPath = "com.leobenkel.zparkioProjectExample.Main",
        jarArgument = Map.empty
      )

      val exitCode = s.execute
      assert(exitCode == 0)
    }
  }
}
