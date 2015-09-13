package controllers

import java.io.{PrintWriter, StringWriter}
import java.nio.file.FileSystems

import play.api.mvc._
import play.api.cache._
import twitter4j._
import views.html.defaultpages.badRequest

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

      Ok(msg + "send success")
    }
    catch {
      case e:TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
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
  def randomTweetImage = Action{
    val twitter = new TwitterFactory().getInstance
    val abPathOfProject = "/Users/String/scala/TwitterBot"
    val file = FileSystems.getDefault.getPath(abPathOfProject + "/images/aizu.gif").toFile



    try {
      new TwitterFactory().getInstance.updateStatus(new StatusUpdate("").media(file))

      Ok("tweet successfully")
    }
    catch {
      case e:TwitterException => BadRequest(e.getStatusCode + ": " + e.getErrorMessage)
    }
  }
}
