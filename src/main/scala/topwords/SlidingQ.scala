package topwords
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.io.Source
import scala.util.Try

import scala.language.unsafeNulls


object SlidingQ {
  private val LAST_N_WORDS = 10

  def main(args: Array[String]): Unit = {
    // Argument validity checking
    if (args.length > 1) {
      Console.err.println("usage: ./target/universal/stage/bin/consoleapp [ last_n_words ]")
      sys.exit(2)
    }

    // Parse command-line argument or use default value
    val lastNWords = if (args.length == 1) {
      Try(args(0).toInt).toOption match {
        case Some(value) if value > 0 => value
        case _ =>
          Console.err.println("argument should be a natural number")
          sys.exit(4)
      }
    } else {
      LAST_N_WORDS
    }

    // Create a CircularFifoQueue to hold the last N words
    val queue = new CircularFifoQueue[String](lastNWords)

    // Print the prompt to the user
    println("Enter number:")

    // Use Source.stdin.getLines() to read input
    Source.stdin.getLines().foreach { line =>
      // Safely check if the line is null or empty
      if (Option(line).getOrElse("").trim.isEmpty) {
        println("Empty input detected, exiting...")
        sys.exit(0) // Gracefully terminate the program
      }

      // Manually split line into words
      val words = manuallySplitIntoWords(line)

      words.foreach { word =>
        queue.add(word) // Oldest item is evicted automatically
        println(queue)

        // Terminate on I/O error (e.g., SIGPIPE)
        if (Console.out.checkError()) sys.exit(1)
      }
    }
  }

  // Method to manually split a line into words based on spaces and punctuation
  private def manuallySplitIntoWords(line: String): Seq[String] = {
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
  }
}

