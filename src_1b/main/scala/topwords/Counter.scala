package topwords
import scala.collection.mutable.Map


trait Counter {
  def account(word: String) : Int
  def getWords() : Map[String, Int]
  def getWordCount(word: String) : Int
  def getWordCount() : Int
  def decrement(word: String): Unit
}