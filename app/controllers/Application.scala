package controllers

import java.io._
import java.nio.file.FileSystems
import java.time.Duration

import apple.laf.JRSUIUtils.Images
import play.api.mvc._
import slick.dbio.DBIO
import twitter4j._
import slick._
import scala.slick._
import scala.concurrent._
import slick.driver.MySQLDriver.api._

import play.api.db.DB

class Application extends Controller {

  def hello = Action{
    Ok(views.html.index("Your new Application is ready."))
  }

  def index = Action {
    val twitter = new TwitterFactory().getInstance
    val user = twitter.verifyCredentials

    val name = user.getName
    val screen = user.getScreenName
    val follow = user.getFriendsCount
    val follower = user.getFollowersCount

    Ok("Current client: \n" + name + " " + " @" + screen + " " + follow + " " + follower + "\n")
  }

  def tweet(msg: String) = Action {
    try {
      new TwitterFactory().getInstance.updateStatus(new StatusUpdate(msg))

      Ok(msg + "\nsend success")
    }
    catch {
      case e:TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
    }
  }

  def tweet(msg: String, file: File) {
    try {
      new TwitterFactory().getInstance.updateStatus(new StatusUpdate(msg).media(file))
    }
    catch {
      case e:TwitterException => throw e
    }
  }

  // command
  // curl -X GET http://localhost:9000/followAndRemove
  def followAndRemove = Action{
    val twitter = new TwitterFactory().getInstance
    val following = twitter.getFriendsIDs(twitter.getId, -1).getIDs.toList
    val followers = twitter.getFollowersIDs(twitter.getId, -1).getIDs.toList

    val result =
      if(following.length == followers.length){
        "All followers are my friends!!!\n"
      }
      else if(following.length > followers.length){
        (following diff followers).foreach(twitter.destroyFriendship(_))
        "Removed: \n" + (following diff followers).mkString("\n")
      }
      else{
        (following diff followers).foreach(twitter.createFriendship(_))
        "Followed: \n" + (followers diff following).mkString("\n")
      }

    Ok(result)
  }

  // command
  // curl -X GET http://localhost:9000/randTweetWithImage
  def tweetWithRandomImage = Action{
    val twitter = new TwitterFactory().getInstance

    val db = Database.forURL(
      "jdbc:mysql://localhost/tweetimagedb?user=root&password=",
      driver = "com.mysql.jdbc.Driver"
    )

    val files = new File("images/").listFiles().map(_.getName).toList.collect {
      case x if (x.endsWith(".gif")) => x
      case x if (x.endsWith(".jpeg")) => x
      case x if (x.endsWith(".jpg")) => x
      case x if (x.endsWith(".png")) => x
      case _ => null
    }.filter(_ != null).zipWithIndex

    println(files.mkString("\n"))

    val images: TableQuery[Images] = TableQuery[Images]
    try {
      db.run {
        val insertImageQ = images ++= files.map {
          tup: (String, Int) => (tup._2 + 1, tup._1)
        }

        //insert action
        DBIO.seq(
          images.schema.drop,
          images.schema.create,
          insertImageQ
        )
      }
    }
    catch {
      case e:ExecutionException => BadRequest("Exception: Execution exception")
    }
    finally{
     db.close
    }

    //idの範囲のランダムな数字を発生、そのidに紐付けされている画像を選択して添付
    val res = images.length
    println(res)
    Ok("test")
/*
    try {
      val file = FileSystems.getDefault.getPath("images/" + imageFile).toFile
      tweet("No.1", file)

      Ok("tweet successfully with image")
    }
    catch{
      case e: TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
    }
*/
  }
}
