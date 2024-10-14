
package topwords

trait QueueManager {
  def addWord(word: String): Unit
  def evictOldest(): (Option[String], QueueManager)
  def isFull(): Boolean
}
