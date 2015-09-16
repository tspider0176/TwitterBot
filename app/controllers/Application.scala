package controllers

import java.io._
import java.nio.file.FileSystems
import java.text.SimpleDateFormat
import scala.concurrent.duration.Duration

import play.api.mvc._
import slick.dbio.DBIO
import twitter4j._
import twitter4j.Status
import scala.concurrent._
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

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

  // command
  // curl -X GET http://localhost:9000/tweet/[tweet sentence]
  def tweet(msg: String) = Action {
    try {
      new TwitterFactory().getInstance.updateStatus(new StatusUpdate(msg))

      Ok(msg + "\nsend success")
    }
    catch {
      case e:TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
      case e:Exception => throw e
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

      val imageIdQ = sql"SELECT IMAGE_ID FROM IMAGES ORDER BY RAND() LIMIT 1".as[String]
      val imageId = Await.result(db.run(imageIdQ), Duration.Inf)

      val idFilter = Compiled { k: Rep[Int] =>
        images.filter(_.id === k)
      }

      Await.result(
        db.run(
          idFilter(imageId(0).toInt).result.map { r =>
            println("DEBUG: Seq (Vector) of selected column")
            println("- " + r.head._1 + " + " + r.head._2)

            try {
              val file = FileSystems.getDefault.getPath("images/" + r.head._2).toFile
              new TwitterFactory().getInstance.updateStatus(new StatusUpdate("No." + r.head._1).media(file))
            }
            catch {
              case e: TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
              case e: Exception => BadRequest(e.getStackTrace.toString)
            }
          }
        ), Duration.Inf
      )

      Ok("tweet with image successfully")
    }
    catch {
      case e:ExecutionException => BadRequest("execution exception" + e.getStackTrace.toString)
      case e:Exception => BadRequest("exception: " + e.getMessage)
    }
    finally {
      db.close
    }
  }

  //TODO
  //ここで自分のタイムラインを取得
  //特定のキーワードをつぶやいてる人がいたらその人に対してリプを飛ばす
  //tweetWithRandomImageの仕様を変更してユーザーにリプを飛ばすようにする
  // command
  // curl -X GET http://localhost:9000/tweetBot
  def getTimeLine = Action{
    try {
      val twitter = new TwitterFactory().getInstance
      val htl = twitter.getMentionsTimeline
      

    }
    catch{
      case e: TwitterException => BadRequest("Twitter Exception" + e.getErrorMessage)
    }

    Ok("test")
  }
}
