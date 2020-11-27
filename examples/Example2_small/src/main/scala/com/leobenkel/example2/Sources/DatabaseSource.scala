package com.leobenkel.example2.Sources

import com.leobenkel.zparkio.Services.SparkModule
import com.leobenkel.zparkio.implicits.ZDS_R
import com.leobenkel.example2.Items.{Post, User}
import com.leobenkel.example2.Services.Database
import com.leobenkel.example2.Services.Database.Database

object DatabaseSource {

  def getUsers: ZDS_R[Database, User] = {
    for {
      spark <- SparkModule()
      users <- {
        import spark.implicits._
        Database[User]("SELECT * FROM users")
      }
    } yield {
      users
    }
  }

  def getPosts: ZDS_R[Database, Post] = {
    for {
      spark <- SparkModule()
      users <- {
        import spark.implicits._
        Database[Post]("SELECT * FROM posts")
      }
    } yield {
      users
    }
  }

}
