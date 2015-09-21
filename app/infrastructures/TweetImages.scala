package infrastructures

import slick.driver.MySQLDriver.api._
import slick.lifted._
/**
 * Created by String on 15/09/13.
 */
class TweetImages(tag: Tag) extends Table[(Int, String)](tag, "IMAGES") {
    def id = column[Int]("IMAGE_ID", O.PrimaryKey)
    def filename  = column[String]("FILE_NAME")

    def * = (id, filename)
}
