package com.leobenkel.zparkiotest

case class SparkExecution(
  private val sparkConf:     Map[String, Any],
  private val sparkFile:     Map[String, String],
  private val pathToJar:     String,
  private val mainClassPath: String,
  private val jarArgument:   Map[String, Option[Any]],
  private val deployMode:    String = "client"
) {

  @transient lazy private val args: String = jarArgument
    .map { case (k, v) => s"--$k ${v.map(_.toString).getOrElse("")}" }
    .mkString("\n")

  @transient lazy private val conf: String = sparkConf
    .map { case (k, v) => s"--conf $k=${v.toString}" }
    .mkString("\n")

  @transient lazy private val files: String = sparkFile
    .map { case (k, v) => s"--files $k#$v" }
    .mkString("\n")

  /**
    * --conf spark.driver.bindAddress=127.0.0.1
    * --conf spark.driver.port=46879
    *
    * @return
    */
  private def makeCommand: List[String] = {
    s"""spark-submit
       |--class $mainClassPath
       |--master local[*]
       |$files
       |$conf
       |file://$pathToJar
       |$args
       |""".stripMargin.split("\\s").filter(_.nonEmpty).toList
  }

  def execute: Int = {
    import sys.process._
    val command = makeCommand
    println(s"Execute:\n${command.mkString("\n")}")
    Process(command, None, "SPARK_LOCAL_IP" -> "127.0.0.1").!(new ProcessLogger {
      override def out(s:       => String): Unit = System.out.println(s)
      override def err(s:       => String): Unit = System.err.println(s)
      override def buffer[T](f: => T):      T = f
    })
  }
}
