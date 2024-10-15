
package topwords

trait QueueManager {
  def addWord(word: String): QueueManager
  def evictOldest(): (Option[String], QueueManager)
  def isFull(): Boolean
}
