package com.leobenkel.zparkioProfileExampleMoreComplex.Transformations

import com.leobenkel.zparkio.implicits.{ZDS, ZDS_R}
import com.leobenkel.zparkioProfileExampleMoreComplex.Items.User
import com.leobenkel.zparkioProfileExampleMoreComplex.Services.Database
import com.leobenkel.zparkioProfileExampleMoreComplex.Sources.DatabaseSource
import org.apache.spark.broadcast.Broadcast

object UserTransformations {

  def getAuthors: ZDS_R[Database, User] = {
    for {
      /* One advantage of ZIO here is the forking of the source fetch.
       * All source can be fetch in parallel.
       * Without ZIO, spark would just seat there while waiting for the first source to be retrieve
       * before sending the next query to the database.
       */
      usersF <- DatabaseSource.getUsers.fork
      postsF <- DatabaseSource.getPosts.fork
      users  <- usersF.join
      posts  <- postsF.join
      authors <- ZDS { spark =>
        import spark.implicits._
        val authorIds: Broadcast[Array[Int]] =
          spark.sparkContext.broadcast(posts.map(_.authorId).distinct.collect)
        users.filter(u => authorIds.value.contains(u.userId))
      }
    } yield {
      authors
    }
  }
}
