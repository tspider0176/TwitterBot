package controllers

import java.io._
import java.nio.file.FileSystems
import java.text.SimpleDateFormat
import models.TweetImage

import scala.concurrent.duration.Duration

import play.api.mvc._
import play.api.cache._
import play.api.Play.current
import play.api.libs.iteratee._

import slick.dbio.DBIO
import twitter4j._
import twitter4j.Status
import scala.concurrent._
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

import infrastructures._

//TODO
//Controller肥大化に伴うリファクタリング
//MVCモデル
class Application extends Controller {

  def hello = Action{
    Redirect("index")
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
  // curl -H "Content-Type: text/plain" -d 'message' http://localhost:9000/tweet
  def tweet = Action{ request=>
    try{
      val body: AnyContent = request.body
      val textBody: Option[String] = body.asText
      new TwitterFactory().getInstance.updateStatus(new StatusUpdate(textBody.get))

      Ok("tweet send success")
    }
    catch{
      case e: TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
      case e: Exception => BadRequest
    }
  }

  // command
  // curl -X DELETE http://localhost:9000/delete/[tweet status id]
  def delete(statId: Long) = Action{
    try{
      val twitter = new TwitterFactory().getInstance

      twitter.destroyStatus(statId)

      Ok("tweet deletion success")
    }
    catch{
      case e: TwitterException => BadRequest
    }
  }

  // command
  // curl -X GET http://localhost:9000/timeline
  def getTimeLine = Action{ request =>
    val twitter = new TwitterFactory().getInstance
    val htl = twitter.getHomeTimeline

    val timeLine = for{
      i <- 0 to htl.size-1
    } yield (htl.get(i).getUser.getScreenName, htl.get(i).getText)

    val escTL = timeLine.toList.map { tup: (String, String) =>
      tup._2 match {
        case x if x.contains("<") => (tup._1, x.replaceAll("<", "&lt;"))
        case x if x.contains(">") => (tup._1, x.replaceAll(">", "&gt;"))
        case x if x.contains("&") => (tup._1, x.replaceAll("&", "&amp;"))
        case x if x.contains("\"") => (tup._1, x.replaceAll("\"", "&quot;"))
        case _ => (tup._1, tup._2)
      }
    }

    Ok(views.html.index(escTL))
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
  // curl -X GET http://localhost:9000/tweetWithRandImg
  def tweetWithRandomImage = Action{
    try{
      TweetImages.registerImageToDB

      try {
        val file = FileSystems.getDefault.getPath("images/" + TweetImages.getRandImage.filename).toFile
        val statUpdate = new StatusUpdate("No." + TweetImages.getRandImage.id).media(file)
        new TwitterFactory().getInstance.updateStatus(statUpdate)
      }
      catch {
        case e: TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
        case e: Exception => BadRequest(e.getStackTrace.toString)
      }

      Ok("tweet with image successfully")
    }
    catch {
      case e:ExecutionException => BadRequest("execution exception" + e.getStackTrace.toString)
      case e:Exception => BadRequest("exception: " + e.getLocalizedMessage + "\n" + e.getMessage)
    }
  }

  def tweet(msg: String) = {
    try {
      new TwitterFactory().getInstance.updateStatus(new StatusUpdate(msg))
    }
    catch {
      case e:TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
      case e:Exception => throw e
    }
  }

  // command
  // curl -X GET http://localhost:9000/tweetBot
  def tweetBot = Action{
    try {
      val twitter = new TwitterFactory().getInstance
      val htl = twitter.getHomeTimeline

      val specifyTweet = for{
        i <- 0 to htl.size-1
      } yield htl.get(i).getText match{
        case x if x.contains("スタイル") => (htl.get(i).getId, htl.get(i).getUser.getScreenName)
        case _ => (0L, null)
      }

      //TODO
      //現在20件の取得制限があるため取得漏れがありえる
      //前回取得したツイート以降のツイートを取得等（最後に読み取ったidを記録するなど）
      //上が実装できれば問題無いが、逆に取得重複してしまう可能性あり
      //status idが重複している場合リストから削除するなど対策必要
      val mRepStatId = specifyTweet.filter(_ != (0L, null)).toList
      println("DEBUG: reply list: \n" + mRepStatId)

      for(in <- mRepStatId) {
        println("DEBUG: reply to: " + in)
        replyWithRandomImage(mRepStatId.head._1, mRepStatId.head._2)
      }

      Ok("success")
    }
    catch{
      case e: TwitterException => BadRequest("Twitter Exception: " + e.getErrorMessage)
      case e: Exception => BadRequest
    }
  }

  def replyWithRandomImage(repToId: Long, screenName: String) :Unit= {
    try {
      TweetImages.registerImageToDB

      try {
        val file = FileSystems.getDefault.getPath("images/" + TweetImages.getRandImage.filename).toFile
        val statUpdate = new StatusUpdate("No." + TweetImages.getRandImage.id).media(file)
        new TwitterFactory().getInstance.updateStatus(statUpdate)
      }
      catch {
        case e: TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
        case e: Exception => BadRequest(e.getStackTrace.toString)
      }
    }
    catch {
      case e:ExecutionException => BadRequest("execution exception" + e.getStackTrace.toString)
      case e:Exception => BadRequest("exception: " + e.getMessage)
    }
  }
}