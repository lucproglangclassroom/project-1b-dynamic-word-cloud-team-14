package impl

import topwords.QueueManager
import org.log4s._
import scala.language.unsafeNulls

class InputProcessorImpl extends topwords.InputProcessor {
  private[this] val logger = org.log4s.getLogger

  // Process a line and return a new word counter and queue
  def processLine(line: String, wordCounter: MapCounter, queue: QueueManagerImpl,
                  cloudSize: Int, lengthAtLeast: Int, windowSize: Int): (MapCounter, QueueManagerImpl) = {
    if (line.trim.nonEmpty) {
      logger.debug(s"Processing line: $line")
      val words = manuallySplitIntoWords(line)

      // Process words and maintain immutable state
      val (updatedCounter, updatedQueue) = words.foldLeft((wordCounter, queue)) {
        case ((currentCounter, currentQueue), word) if word.length >= lengthAtLeast =>
          logger.debug(s"Word passed length filter: $word")

          // Evict and decrement word count if the window size is exceeded
          val (newQueue, decrementedCounter) = if (currentQueue.isFull()) {
            val (evictedWord, newQueue) = currentQueue.evictOldest()
            evictedWord.fold((currentQueue, currentCounter)) { ew =>
              logger.debug(s"Evicting word: $ew")
              (newQueue, safeDecrement(ew, currentCounter))
            }
          } else {
            (currentQueue, currentCounter)
          }

          // Add word to the queue and update the counter
          val nextQueue = newQueue.addWord(word).asInstanceOf[QueueManagerImpl]
          val nextCounter:MapCounter = decrementedCounter.account(word)._1 // Update the word counter
          logger.debug(s"Word added to queue and counted: $word")

          (nextCounter, nextQueue)

        case (state, _) => state // Skip words that do not meet length criteria
      }

      (updatedCounter, updatedQueue)
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
    delimiterPattern.findAllIn(line).toSeq
  }

  private def safeDecrement(word: String | Null, wordCounter: MapCounter): MapCounter = {
    Option(word).map { w =>
      logger.debug(s"Decrementing word count for: $w")
      wordCounter.decrement(w).asInstanceOf[MapCounter]
    }.getOrElse(wordCounter)
  }
}



