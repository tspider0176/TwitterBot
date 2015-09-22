package infrastructures

import java.io.File

import slick.dbio.DBIO
import slick.driver.MySQLDriver.api._

import scala.concurrent._
import scala.concurrent.duration.Duration

import models._

import java.util.Random

/**
 * Created by String on 15/09/13.
 */
class TweetImages(tag: Tag) extends Table[TweetImage](tag, "IMAGES") {
    def id = column[Int]("IMAGE_ID", O.PrimaryKey)
    def filename  = column[String]("FILE_NAME")

    def * = (id, filename) <> (TweetImage.tupled, TweetImage.unapply)
}

object TweetImages extends DAO {
  val db = Database.forURL(
    "jdbc:mysql://localhost/tweetimagedb?user=root&password=",
    driver = "com.mysql.jdbc.Driver"
  )

  def insert(image: TweetImage):Unit ={
    try {
      Await.result(
        db.run(
          TweetImages += image
        ), Duration.Inf
      )
    }
    catch{
      case e: ExecutionException => println(e.getMessage)
      case e: Exception => println(e.getMessage)
    }
  }

  def findById(id: Int): Seq[TweetImage] ={
    try {
      Await.result(
        db.run(
          TweetImages filter {
            _.id === id
          } result
        ), Duration.Inf
      )
    }
    catch{
      case e: ExecutionException => throw e
      case e: Exception => throw e
    }
  }

  def registerImageToDB:Unit ={
    val files = new File("images/").listFiles().map(_.getName).toList.collect {
      case x if x.endsWith(".gif") => x
      case x if x.endsWith(".jpeg") => x
      case x if x.endsWith(".jpg") => x
      case x if x.endsWith(".png") => x
      case _ => null
    }.filter(_ != null).zipWithIndex

    try {
      files.foreach {
        ti: (String, Int) => insert(TweetImage(ti._2 + 1, ti._1))
      }
    }
    catch {
      case e:ExecutionException => println("DEBUG: " + e.getMessage)
      case e:Exception => println("DEBUG: " + e.getMessage)
    }
  }

  def getRandImage: TweetImage ={
    try {
      val id = new Random().nextInt(
        Await.result(
          db.run(
            TweetImages.length.result
          ),Duration.Inf
        )
      ) + 1

      findById(id).head
    }
    catch{
      case e: ExecutionException => throw e
      case e: Exception => throw e
    }
  }
}
