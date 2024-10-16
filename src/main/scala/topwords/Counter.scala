
package topwords
import scala.collection.immutable.Map


trait Counter {
  def account(word: String): (Counter, Int)
  def getWords() : Map[String, Int]
  def increment(word: String): Counter
  def getWordCount(word: String) : Int
  def getWordCount() : Int
  def decrement(word: String): Counter
}
