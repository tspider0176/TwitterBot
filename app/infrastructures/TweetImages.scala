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
