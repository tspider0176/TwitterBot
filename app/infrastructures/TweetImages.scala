package infrastructures

import java.io.File

import slick.dbio.DBIO
import slick.driver.MySQLDriver.api._

import scala.concurrent._
import scala.concurrent.duration.Duration

import models._

/**
 * Created by String on 15/09/13.
 */
class TweetImages(tag: Tag) extends Table[TweetImage](tag, "IMAGES") {
    def id = column[Int]("IMAGE_ID", O.PrimaryKey)
    def filename  = column[String]("FILE_NAME")

    def * = (id, filename) <> (TweetImage.tupled, TweetImage.unapply)
}

class TweetImageDB {
  def registerImage: Unit ={
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
            ti : (String, Int) => TweetImage(ti._2 + 1, ti._1)
          }.toIterable

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

  def getRandImage: Vector[String]= {
    val db = Database.forURL(
      "jdbc:mysql://localhost/tweetimagedb?user=root&password=",
      driver = "com.mysql.jdbc.Driver"
    )

    try {
      val imageIdQ = sql"SELECT IMAGE_ID FROM IMAGES ORDER BY RAND() LIMIT 1".as[String]
      Await.result(db.run(imageIdQ), Duration.Inf)
    }
    catch{
      case e: ExecutionException => throw e
      case e: Exception => throw e
    }
    finally{
      db.close
    }
  }
}