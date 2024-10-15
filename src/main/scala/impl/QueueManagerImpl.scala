
package impl

import topwords.QueueManager //importing traits from defined interface
import scala.language.unsafeNulls
import org.log4s._   // For logging functionality.


//The mutable CircularFifoQueue is replaced by an immutable List
// A purely functional implementation of the QueueManager.
class QueueManagerImpl(val windowSize: Int, val queue: List[String] = List.empty) extends topwords.QueueManager {
  private val logger = getLogger

  //the logic of adding and removing elements now returns new instances of QueueManagerImpl instead of modifying the state in place
  // Adding a word returns a new instance of QueueManagerImpl with the updated queue.
  override def addWord(word: String): QueueManager = {
    val newQueue =
      if (queue.size >= windowSize) queue.tail :+ word
      else queue :+ word


    if (newQueue.contains(word)) {
      logger.debug(s"Successfully added word: $word to the queue")
    } else {
      logger.warn(s"Failed to add word: $word to the queue, queue may be full")
    }

    new QueueManagerImpl(windowSize, newQueue)
  }


  // Evicts the oldest word (the first word in the queue) and returns a new queue without it.
  override def evictOldest(): (Option[String], QueueManager) = queue match {
    case Nil => (None, this) // No word to evict
    case head :: tail =>
      (Some(head), new QueueManagerImpl(windowSize, tail))
  }


  // Check if the queue is full
  //Used override to returns a new instance of QueueManagerImpl, which represents the new state of the queue.
  override def isFull(): Boolean = queue.size == windowSize
}
