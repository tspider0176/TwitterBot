package models

import infrastructures.TweetImages
import slick.lifted.TableQuery

/**
 * Created by String on 15/09/22.
 */
class DAO {
  val TweetImages = TableQuery[TweetImages]
}
