package infrastructures

import java.io.File
import java.util.Random

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

object TweetImages extends DAO {
  val db = Database.forURL(
    "jdbc:mysql://localhost/tweetimagedb?user=root&password=",
    driver = "com.mysql.jdbc.Driver"
  )

  // Create
  def insert(image: TweetImage): Unit = {
    try {
      Await.result(
        db.run(
          TweetImages += image
        ), Duration.Inf
      )
    }
    catch {
      case e: ExecutionException => println(e.getMessage)
      case e: Exception => println(e.getMessage)
    }
  }

  // Read
  // SELECT * FROM TweetImages
  def select(): Seq[TweetImage] = {
    try{
      Await.result(
        db.run(
          TweetImages.result
        ), Duration.Inf
      )
    }
    catch{
      case e: ExecutionException => throw e
      case e: Exception => throw e
    }
  }

  // Read
  // SELECT * FROM TweetImages WHERE id == [id]
  def findById(id: Int): Seq[TweetImage] ={
    try {
      Await.result(
        db.run(
          TweetImages.filter(_.id === id).result
        ), Duration.Inf
      )
    }
    catch{
      case e: ExecutionException => throw e
      case e: Exception => throw e
    }
  }

  // Update
  // UPDATE TweetImages SET [TweetImage] WHERE id == [id]
  def updateId(id: Int, ti: TweetImage): Unit = {
    try {
      Await.result(
        db.run(
          TweetImages.filter(_.id === id).update(ti)
        ), Duration.Inf
      )
    }
    catch {
      case e: ExecutionException => println(e.getMessage)
      case e: Exception => println(e.getMessage)
    }
  }

  // Delete (all element)
  // DELETE FROM TweetImages
  def deleteAll():Unit ={
    try {
      Await.result(
        db.run(
          TweetImages.delete
        ), Duration.Inf
      )
    }
    catch{
      case e: ExecutionException => println(e.getMessage)
      case e: Exception => println(e.getMessage)
    }
  }

  // Delete (designate id)
  // DELETE FROM TweetImages WHERE id == [id]
  def deleteId(id: Int):Unit={
    Await.result(
      db.run(
        TweetImages.filter(_.id === id).delete
      ),Duration.Inf
    )
  }

  def closeDB: Unit ={
    db.close
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
