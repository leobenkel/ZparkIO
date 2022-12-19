package com.leobenkel.example2.Transformations

import com.leobenkel.example2.Items.User
import com.leobenkel.example2.Services.Database.Database
import com.leobenkel.example2.Sources.DatabaseSource
import com.leobenkel.zparkio.implicits.{ZDS, ZDS_R}

object UserTransformations {
  def getAuthors: ZDS_R[Database, User] =
    for {
      /* One advantage of ZIO here is the forking of the source fetch.
       * All source can be fetch in parallel.
       * Without ZIO, spark would just seat there while waiting for the first source to be retrieve
       * before sending the next query to the database.
       */
      usersF    <- DatabaseSource.getUsers.fork
      postsF    <- DatabaseSource.getPosts.fork
      users     <- usersF.join
      posts     <- postsF.join
      authorIds <-
        ZDS.broadcast { spark =>
          import spark.implicits._
          posts.map(_.authorId).distinct().collect()
        }
      authors   <- ZDS(_ => users.filter(u => authorIds.value.contains(u.userId)))
    } yield authors
}
