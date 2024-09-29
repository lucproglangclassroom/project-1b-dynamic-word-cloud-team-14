package impl

import org.log4s._
import scala.collection.mutable.Map

class MapCounter extends topwords.Counter {

  private val logger = getLogger("Main")// $COVERAGE-OFF$
  private val scores = scala.collection.mutable.Map[String, Int]()

  // Method to add or update the count of a word
  def account(word: String): Int = {
    if (scores.contains(word)) {
      scores(word) += 1
      logger.debug(s"Incremented count for word: $word, new count: ${scores(word)}")// $COVERAGE-OFF$
    } else {
      scores(word) = 1
      logger.debug(s"Added new word: $word with count: 1")// $COVERAGE-OFF$
    }
    getWordCount(word)
  }

  // Method to get the map of all words and their counts
  def getWords(): Map[String, Int] = {
    logger.debug(s"Returning all words with counts: ${scores.mkString(", ")}")// $COVERAGE-OFF$
    scores
  }

  // Method to get the count of a specific word
  def getWordCount(word: String): Int = {
    val count = scores.getOrElse(word, -1)
    logger.debug(s"Retrieved count for word: $word, count: $count")// $COVERAGE-OFF$
    count
  }

  // Method to get the total number of distinct words
  def getWordCount(): Int = {
    val size = scores.size
    logger.debug(s"Total distinct word count: $size")// $COVERAGE-OFF$
    size
  }

  // Method to increment the word count
  def increment(word: String): Unit = {
    val newCount = scores.getOrElse(word, 0) + 1
    scores.put(word, newCount)
    logger.debug(s"Incremented word: $word, new count: $newCount")// $COVERAGE-OFF$
  }

  // Method to decrement the word count and remove it if the count reaches 0
  def decrement(word: String): Unit = {
    if (scores.contains(word)) {
      scores(word) -= 1
      logger.debug(s"Decremented count for word: $word, new count: ${scores(word)}")// $COVERAGE-OFF$

      if (scores(word) <= 0) {
        scores.remove(word)
        logger.debug(s"Removed word: $word as its count reached 0")// $COVERAGE-OFF$
      }
    }
  }
}
