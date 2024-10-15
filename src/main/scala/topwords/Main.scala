package topwords

import org.log4s._
import mainargs.{main, arg, ParserForMethods}
import scala.io.Source
import impl.{InputProcessorImpl, MapCounter, QueueManagerImpl}
import scala.language.unsafeNulls

object Main:
  private[this] val logger = getLogger

  @main
  def run(
           @arg(name = "cloud-size", short = 'c', doc = "Number of top words to display") cloudSize: Int,
           @arg(name = "length-at-least", short = 'l', doc = "Minimum length of words") lengthAtLeast: Int,
           @arg(name = "window-size", short = 'w', doc = "Size of the moving window for recent words") windowSize: Int
         ): Unit =

    if (cloudSize <= 0 || lengthAtLeast <= 0 || windowSize <= 0) {
      logger.error("Invalid arguments: all arguments must be positive numbers.")
      throw new IllegalArgumentException("All arguments must be positive numbers.")
    }

    logger.info(s"Starting with cloudSize: $cloudSize, lengthAtLeast: $lengthAtLeast, windowSize: $windowSize")

    val inputProcessor = new InputProcessorImpl()
    val initialWordCounter = new MapCounter()
    val initialQueueManager = new QueueManagerImpl(windowSize)

    if (System.in.available() > 0) {
      logger.info("Reading input from pipe")
      processInput(initialQueueManager, initialWordCounter, inputProcessor, cloudSize, lengthAtLeast)
    } else {
      println("Enter text (Ctrl+D to exit):")
      processInput(initialQueueManager, initialWordCounter, inputProcessor, cloudSize, lengthAtLeast)
    }

    println("Final word cloud:")
    printWordCloud(initialWordCounter, cloudSize)

  private def processInput(queueManager: QueueManagerImpl, wordCounter: MapCounter,
                           inputProcessor: InputProcessorImpl, cloudSize: Int, lengthAtLeast: Int): Unit = {

    Source.stdin.getLines().foldLeft((wordCounter, queueManager)) { (acc, line) =>
      val (currentWordCounter, currentQueueManager) = acc
      val (updatedWordCounter, updatedQueueManager) = inputProcessor.processLine(line, currentWordCounter,
        currentQueueManager, cloudSize, lengthAtLeast, currentQueueManager.windowSize)

      printWordCloud(updatedWordCounter, cloudSize)
      (updatedWordCounter, updatedQueueManager)
    }
  }

  private def printWordCloud(counter: MapCounter, cloudSize: Int): Unit = {
    val sortedWords = counter.getWords().toSeq.sortBy(-_._2).take(cloudSize)
    val wordCloud = sortedWords.map { case (word, count) => s"$word: $count" }.mkString(" ")
    logger.info(s"Word cloud: $wordCloud")
    println(wordCloud)
  }

  def main(args: Array[String]): Unit = {
    logger.info("Application started")
    ParserForMethods(this).runOrExit(args)
    logger.info("Application finished")
  }
end Main




