package controllers

import slick.driver.MySQLDriver.api._

/**
 * Created by String on 15/09/13.
 */
class Images(tag: Tag) extends Table[(Int, String)](tag, "IMAGES") {
    def id = column[Int]("IMAGE_ID", O.PrimaryKey) // 主キー
    def filename = column[String]("FILE_NAME")
    def * = (id, filename)
}
