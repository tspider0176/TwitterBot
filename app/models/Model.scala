package models

import java.io.File

import infrastructures.TweetImages
import slick.dbio.DBIO
import slick.driver.MySQLDriver.api._

import scala.concurrent._
import scala.concurrent.duration.Duration

/**
 * Created by String on 15/09/21.
 */

object TweetImageDB {
  def getAndRegister: Unit ={
    val db = Database.forURL(
      "jdbc:mysql://localhost/tweetimagedb?user=root&password=",
      driver = "com.mysql.jdbc.Driver"
    )

    val files = new File("images/").listFiles().map(_.getName).toList.collect {
      case x if x.endsWith(".gif") => x
      case x if x.endsWith(".jpeg") => x
      case x if x.endsWith(".jpg") => x
      case x if x.endsWith(".png") => x
      case _ => null
    }.filter(_ != null).zipWithIndex

    val images: TableQuery[TweetImages] = TableQuery[TweetImages]
    try {

      Await.result(
        db.run {
          val insertImageQ = images ++= files.map {
            tup: (String, Int) => (tup._2 + 1, tup._1)
          }
          DBIO.seq(
            images.schema.drop,
            images.schema.create,
            insertImageQ
          )
        }, Duration.Inf
      )
    }
    catch {
      case e:ExecutionException => println("DEBUG: " + e.getMessage)
      case e:Exception => println("DEBUG: " + e.getMessage)
    }
    finally {
      db.close
    }
  }


}
