package topwords

import org.log4s._
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.io.Source
import impl.MapCounter
import mainargs.{main, arg, ParserForMethods, Flag}
import scala.language.unsafeNulls

object Main:
  private [this] val logger = org.log4s.getLogger

  @main
  def run(
           @arg(name = "cloud-size", short = 'c', doc = "Number of top words to display") cloudSize: Int,
           @arg(name = "length-at-least", short = 'l', doc = "Minimum length of words") lengthAtLeast: Int,
           @arg(name = "window-size", short = 'w', doc = "Size of the moving window for recent words") windowSize: Int
         ): Unit =

    // Input Validation
    if (cloudSize <= 0 || lengthAtLeast <= 0 || windowSize <= 0) {
      logger.error("Invalid arguments: all arguments must be positive numbers.")
      Console.err.println("All arguments must be positive numbers.")
      sys.exit(1)
    }

    logger.info(s"Starting with cloudSize: $cloudSize, lengthAtLeast: $lengthAtLeast, windowSize: $windowSize")

    // Create a CircularFifoQueue to hold the last N words
    val queue = new CircularFifoQueue[String](windowSize)

    // Create an instance of MapCounter to track word counts
    val wordCounter = new MapCounter()

    // Print the prompt to the user
    logger.info("Prompting user for input")
    println("Enter text (Ctrl+D to exit):")

    // Use Source.stdin.getLines() to read input
    Source.stdin.getLines().foreach { line =>
      logger.debug(s"Processing line: $line")
      val words = manuallySplitIntoWords(line)

      def safeDecrement(word: String | Null): Unit = {
        Option(word).foreach { w =>
          logger.debug(s"Decrementing word count for: $w")
          wordCounter.decrement(w)
        }
      }

      words.foreach { word =>
        // Filter words based on the length requirement
        if (word.length >= lengthAtLeast) {
          logger.debug(s"Word passed length filter: $word")

          // Handle word eviction from the queue
          if (queue.size == windowSize) {
            val evictedWord = queue.poll()
            logger.debug(s"Evicting word: $evictedWord")
            safeDecrement(evictedWord)
          }

          // Add the new word to the queue and update its count in MapCounter
          queue.add(word)
          wordCounter.account(word)
          logger.debug(s"Word added to queue and counted: $word")

          // Print the word cloud of the top cloudSize words
          printWordCloud(wordCounter, cloudSize)
        }
      }

      // Terminate on I/O error (e.g., SIGPIPE)
      if (Console.out.checkError()) {
        logger.error("I/O error detected, terminating")
        sys.exit(1)
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

  // Helper method to print the word cloud
  private def printWordCloud(counter: MapCounter, cloudSize: Int): Unit = {
    val sortedWords = counter.getWords().toSeq.sortBy(-_._2).take(cloudSize)
    val wordCloud = sortedWords.map { case (word, count) => s"$word: $count" }.mkString(" ")
    logger.info(s"Word cloud: $wordCloud")
    println(wordCloud)
  }

  // Method to manually split a line into words based on spaces and punctuation
  private def manuallySplitIntoWords(line: String): Seq[String] = {
    logger.debug(s"Splitting line: $line")

    val delimiters = Set(' ', ',', '.', ';', ':', '!', '?', '\t', '\n', '\r')
    val currentWord = new StringBuilder
    val words = scala.collection.mutable.ListBuffer[String]()

    for (char <- line) {
      if (delimiters.contains(char)) {
        if (currentWord.nonEmpty) {
          val word = currentWord.toString
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
      val word = currentWord.toString
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
    () // Explicitly return Unit
  }

end Main
