
package topwords

import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.io.Source
import impl.MapCounter
import mainargs.{main, arg, ParserForMethods, Flag}
import scala.language.unsafeNulls

object Main:
  println("Main object initialized")

  @main
  def run(
           @arg(name = "cloud-size", short = 'c', doc = "Number of top words to display") cloudSize: Int,
           @arg(name = "length-at-least", short = 'l', doc = "Minimum length of words") lengthAtLeast: Int,
           @arg(name = "window-size", short = 'w', doc = "Size of the moving window for recent words") windowSize: Int
         ): Unit =
    // Input Validation
    if (cloudSize <= 0 || lengthAtLeast <= 0 || windowSize <= 0) {
      Console.err.println("All arguments must be positive numbers.")
      sys.exit(1)
    }

    // Create a CircularFifoQueue to hold the last N words
    val queue = new CircularFifoQueue[String](windowSize)

    // Create an instance of MapCounter to track word counts
    val wordCounter = new MapCounter()

    // Print the prompt to the user
    println("Enter text (Ctrl+D to exit):")

    // Use Source.stdin.getLines() to read input
    Source.stdin.getLines().foreach { line =>
      // Manually split line into words
      val words = manuallySplitIntoWords(line)

      def safeDecrement(word: String | Null): Unit = {
        Option(word).foreach(wordCounter.decrement)
      }

      words.foreach { word =>
        // Filter words based on the length requirement
        if (word.length >= lengthAtLeast) {
          // Handle word eviction from the queue
          if (queue.size == windowSize) {
            val evictedWord = queue.poll() // Removes and returns the oldest word
            // Decrement count of the evicted word
            safeDecrement(evictedWord)
          }

          // Add the new word to the queue and update its count in MapCounter
          queue.add(word)
          wordCounter.account(word)

          // Print the word cloud of the top cloudSize words
          printWordCloud(wordCounter, cloudSize)
        }
      }

      // Terminate on I/O error (e.g., SIGPIPE)
      if (Console.out.checkError()) sys.exit(1)
    }

  def testSlidingQueue(words: Seq[String], windowSize: Int): MapCounter = {
    val counter = new MapCounter()
    val queue = scala.collection.mutable.Queue[String]()

    for (word <- words) {
      queue.enqueue(word)
      // Use the increment method to update the word count
      counter.increment(word)

      if (queue.size > windowSize) {
        val oldestWord = queue.dequeue()
        counter.decrement(oldestWord)
      }
    }

    counter
  }
  // Helper method to print the word cloud
  private def printWordCloud(counter: MapCounter, cloudSize: Int): Unit = {
    val sortedWords = counter.getWords().toSeq.sortBy(-_._2).take(cloudSize)
    val wordCloud = sortedWords.map { case (word, count) => s"$word: $count" }.mkString(" ")
    println(wordCloud)
  }

  // Method to manually split a line into words based on spaces and punctuation
  private def manuallySplitIntoWords(line: String): Seq[String] =
    val delimiters = Set(' ', ',', '.', ';', ':', '!', '?', '\t', '\n', '\r')
    val currentWord = new StringBuilder
    val words = scala.collection.mutable.ListBuffer[String]()

    for (char <- line) {
      if (delimiters.contains(char)) {
        if (currentWord.nonEmpty) {
          words += currentWord.toString
          currentWord.clear()
        }
      } else {
        currentWord += char
      }
    }

    // Add the last word if there's any remaining
    if (currentWord.nonEmpty) {
      words += currentWord.toString
    }

    words.toList

  // This main method is necessary for the application to run
  def main(args: Array[String]): Unit = {
    ParserForMethods(this).runOrExit(args)
    () // Explicitly return Unit
  }

end Main

