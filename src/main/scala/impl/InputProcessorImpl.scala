package impl

import topwords.QueueManager
import org.log4s._
import scala.language.unsafeNulls

class InputProcessorImpl extends topwords.InputProcessor {
  private[this] val logger = org.log4s.getLogger

  // Modify processLine to return updated queue and word counter
  def processLine(line: String, wordCounter: MapCounter, queue: QueueManagerImpl,
                  cloudSize: Int, lengthAtLeast: Int, windowSize: Int): (MapCounter, QueueManagerImpl) = {
    if (line.trim.nonEmpty) {
      logger.debug(s"Processing line: $line")
      val words = manuallySplitIntoWords(line)

      // Keep track of the updated state
      var currentQueue = queue
      var currentCounter = wordCounter

      words.foreach { word =>
        if (word.length >= lengthAtLeast) {
          logger.debug(s"Word passed length filter: $word")

          // Evict and decrement word count if the window size is exceeded
          if (currentQueue.isFull()) {
            val (evictedWord, newQueue) = currentQueue.evictOldest()
            evictedWord.foreach { ew =>
              logger.debug(s"Evicting word: $ew")
              currentCounter = safeDecrement(ew, currentCounter)
            }
            currentQueue = newQueue.asInstanceOf[QueueManagerImpl]
          }

          currentQueue = currentQueue.addWord(word).asInstanceOf[QueueManagerImpl]
          currentCounter = currentCounter.account(word)._1 // Update the word counter
          logger.debug(s"Word added to queue and counted: $word")
          //printWordCloud(currentCounter, cloudSize)
        }
      }

      (currentCounter, currentQueue)
    } else {
      logger.debug("Empty line detected, skipping.")
      (wordCounter, queue)
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
    val delimiterPattern = "[A-Za-z0-9]+".r
    val words = delimiterPattern.findAllIn(line).toSeq
    words
  }

  private def safeDecrement(word: String | Null, wordCounter: MapCounter): MapCounter = {
    Option(word).map { w =>
      logger.debug(s"Decrementing word count for: $w")

      wordCounter.decrement(w).asInstanceOf[MapCounter]
    }.getOrElse(wordCounter)
  }
}



