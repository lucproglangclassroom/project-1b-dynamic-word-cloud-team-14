
package impl

import topwords.QueueManager
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.language.unsafeNulls
import org.log4s._

class QueueManagerImpl(val windowSize: Int) extends topwords.QueueManager {
  private val queue = new CircularFifoQueue[String](windowSize)
  private val logger = getLogger

  def addWord(word: String): Unit = {
    val added = queue.add(word)
    if (added) {
      logger.debug(s"Successfully added word: $word to the queue")
    } else {
      logger.warn(s"Failed to add word: $word to the queue, queue may be full")
    }
  }

  def evictOldest(): Option[String] = Option(queue.poll())

  def isFull(): Boolean = queue.size == windowSize
}
