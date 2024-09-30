
package topwords

import org.log4s._
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.io.Source
import impl.MapCounter
import mainargs.{main, arg, ParserForMethods}
import scala.language.unsafeNulls

object Main:
  private[this] val logger = org.log4s.getLogger
  // Main entry point for running the program
  @main
  def run(
           @arg(name = "cloud-size", short = 'c', doc = "Number of top words to display") cloudSize: Int,
           @arg(name = "length-at-least", short = 'l', doc = "Minimum length of words") lengthAtLeast: Int,
           @arg(name = "window-size", short = 'w', doc = "Size of the moving window for recent words") windowSize: Int
         ): Unit =

    // Input validation
    if (cloudSize <= 0 || lengthAtLeast <= 0 || windowSize <= 0) {
      logger.error("Invalid arguments: all arguments must be positive numbers.")
      throw new IllegalArgumentException("All arguments must be positive numbers.")
    }

    logger.info(s"Starting with cloudSize: $cloudSize, lengthAtLeast: $lengthAtLeast, windowSize: $windowSize")

    // Check if input is piped or from stdin
    if (System.in.available() > 0) {
      logger.info("Reading input from pipe")
      val wordCounter = new MapCounter()
      val queue = new CircularFifoQueue[String](windowSize)

      Source.stdin.getLines().foreach { line =>
        processLine(line, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)
      }
    } else {
      println("Enter text (Ctrl+D to exit):")

      // Process user input from standard input interactively
      val wordCounter = new MapCounter()
      val queue = new CircularFifoQueue[String](windowSize)

      Source.stdin.getLines().foreach { line =>
        processLine(line, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)
      }
    }

  // Process each input line
  def processLine(line: String, wordCounter: MapCounter, queue: CircularFifoQueue[String],
                  cloudSize: Int, lengthAtLeast: Int, windowSize: Int): Unit = {
    if (line.trim.nonEmpty) {
      logger.debug(s"Processing line: $line")
      val words = manuallySplitIntoWords(line)

      words.foreach { word =>
        if (word.length >= lengthAtLeast) {
          logger.debug(s"Word passed length filter: $word")

          // Evict and decrement word count if the window size is exceeded
          if (queue.size == windowSize) {
            val evictedWord = queue.poll()
            logger.debug(s"Evicting word: $evictedWord")
            safeDecrement(evictedWord, wordCounter)
          }

          queue.add(word)
          wordCounter.account(word)
          logger.debug(s"Word added to queue and counted: $word")
          printWordCloud(wordCounter, cloudSize)
        }
      }
    } else {
      logger.debug("Empty line detected, skipping.")
    }
  }

  def testSlidingQueue(words: Seq[String], windowSize: Int): MapCounter = {
    logger.info(s"Testing sliding queue with window size: $windowSize")
    val counter = new MapCounter()
    val queue = scala.collection.mutable.Queue[String]()

    for (word <- words) {
      logger.debug(s"Processing word: $word")
      queue.enqueue(word)
      counter.increment(word)

      if (queue.size > windowSize) {
        val oldestWord = queue.dequeue()
        logger.debug(s"Decrementing count for oldest word: $oldestWord")
        counter.decrement(oldestWord)
      }
    }
    counter
  }

  // Safely decrement word count in MapCounter
  def safeDecrement(word: String | Null, wordCounter: MapCounter): Unit = {
    Option(word).foreach { w =>
      logger.debug(s"Decrementing word count for: $w")
      wordCounter.decrement(w)
    }
  }

  // Method to print the top N words as a word cloud
  def printWordCloud(counter: MapCounter, cloudSize: Int): Unit = {
    val sortedWords = counter.getWords().toSeq.sortBy(-_._2).take(cloudSize)
    val wordCloud = sortedWords.map { case (word, count) => s"$word: $count" }.mkString(" ")
    logger.info(s"Word cloud: $wordCloud")
    println(wordCloud)
  }

  // Method to manually split a line into words based on spaces and punctuation
  def manuallySplitIntoWords(line: String): Seq[String] = {
    logger.debug(s"Splitting line: $line")
    val delimiters = Set(' ', ',', '.', ';', ':', '!', '?', '\t', '\n', '\r')
    val currentWord = new StringBuilder
    val words = scala.collection.mutable.ListBuffer[String]()

    for (char <- line) {
      if (delimiters.contains(char)) {
        if (currentWord.nonEmpty) {
          val word = currentWord.toString()
          words += word
          logger.debug(s"Extracted word: $word")
          currentWord.clear()
        }
      } else {
        currentWord += char
      }
    }

    // Add the last word if there's any remaining
    if (currentWord.nonEmpty) {
      val word = currentWord.toString()
      words += word
      logger.debug(s"Extracted final word: $word")
    }
    logger.debug(s"Final word list: ${words.mkString(", ")}")
    words.toList
  }

  // This main method is necessary for the application to run
  def main(args: Array[String]): Unit = {
    logger.info("Application started")
    ParserForMethods(this).runOrExit(args)
    logger.info("Application finished")
  }
end Main

