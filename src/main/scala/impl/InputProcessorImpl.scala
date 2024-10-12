
package impl

import topwords.QueueManager
import org.log4s._
import scala.collection.mutable.ListBuffer
import scala.language.unsafeNulls

class InputProcessorImpl extends topwords.InputProcessor{
  private[this] val logger = org.log4s.getLogger


  def processLine(line: String, wordCounter: MapCounter, queue: QueueManager,
                  cloudSize: Int, lengthAtLeast: Int, windowSize: Int): Unit = {
    if (line.trim.nonEmpty) {
      logger.debug(s"Processing line: $line")
      val words = manuallySplitIntoWords(line)

      words.foreach { word =>
        if (word.length >= lengthAtLeast) {
          logger.debug(s"Word passed length filter: $word")

          // Evict and decrement word count if the window size is exceeded
          if (queue.isFull()) {
            val evictedWord = queue.evictOldest()
            evictedWord.foreach { ew =>
              logger.debug(s"Evicting word: $ew")
              safeDecrement(ew, wordCounter)
            }
          }

          queue.addWord(word)
          wordCounter.account(word)
          logger.debug(s"Word added to queue and counted: $word")
          printWordCloud(wordCounter, cloudSize)
        }
      }
    } else {
      logger.debug("Empty line detected, skipping.")
    }
  }


  def printWordCloud(counter: MapCounter, cloudSize: Int): Unit = {
    val sortedWords = counter.getWords().toSeq.sortBy(-_._2).take(cloudSize)
    val wordCloud = sortedWords.map { case (word, count) => s"$word: $count" }.mkString(" ")
    logger.info(s"Word cloud: $wordCloud")
    println(wordCloud)
  }


  def manuallySplitIntoWords(line: String): Seq[String] = {
    logger.debug(s"Splitting line: $line")
    val delimiterPattern = "[A-Za-z0-9]+".r //should match all instances of one or more word characters
    val words = delimiterPattern.findAllIn(line).toSeq //this uses a matchIterator to do the actual work then immediately converts to a seq
    words
  }


  private def safeDecrement(word: String | Null, wordCounter: MapCounter): Unit = {
    Option(word).foreach { w =>
      logger.debug(s"Decrementing word count for: $w")
      wordCounter.decrement(w)
    }
  }
}



