
package impl

import org.log4s._
import scala.collection.mutable.Map

class MapCounter extends topwords.Counter {

  private val logger = getLogger("Main")
  private val scores = scala.collection.mutable.Map[String, Int]()


  def account(word: String): Int = {
    if (scores.contains(word)) {
      scores(word) += 1
      logger.debug(s"Incremented count for word: $word, new count: ${scores(word)}")
    } else {
      scores(word) = 1
      logger.debug(s"Added new word: $word with count: 1")
    }
    getWordCount(word)
  }


  def getWords(): Map[String, Int] = {
    logger.debug(s"Returning all words with counts: ${scores.mkString(", ")}")
    scores
  }


  def getWordCount(word: String): Int = {
    val count = scores.getOrElse(word, -1)
    logger.debug(s"Retrieved count for word: $word, count: $count")
    count
  }


  def getWordCount(): Int = {
    val size = scores.size
    logger.debug(s"Total distinct word count: $size")
    size
  }


  def increment(word: String): Unit = {
    val newCount = scores.getOrElse(word, 0) + 1
    scores.put(word, newCount)
    logger.debug(s"Incremented word: $word, new count: $newCount")
  }


  def decrement(word: String): Unit = {
    if (scores.contains(word)) {
      scores(word) -= 1
      logger.debug(s"Decremented count for word: $word, new count: ${scores(word)}")

      if (scores(word) <= 0) {
        scores.remove(word)
        logger.debug(s"Removed word: $word as its count reached 0")
      }
    }
  }
}
