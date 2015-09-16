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
  // curl -X GET http://localhost:9000/delete/[tweet status id]
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
              val statUpdate = new StatusUpdate("No." + r.head._1).media(file)
              new TwitterFactory().getInstance.updateStatus(statUpdate)
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
    val twitter = new TwitterFactory().getInstance

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
              val statUpdate = new StatusUpdate("@" + screenName + "\nNo." + r.head._1).media(file)
              statUpdate.setInReplyToStatusId(repToId)
              new TwitterFactory().getInstance.updateStatus(statUpdate)
              println("DEBUG: reply to status ID:" + repToId)
            }
            catch {
              case e: TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
              case e: Exception => BadRequest(e.getStackTrace.toString)
            }
          }
        ), Duration.Inf
      )
    }
    catch {
      case e:ExecutionException => BadRequest("execution exception" + e.getStackTrace.toString)
      case e:Exception => BadRequest("exception: " + e.getMessage)
    }
    finally {
      db.close
    }
  }
}
