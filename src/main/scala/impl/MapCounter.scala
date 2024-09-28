package impl

import scala.collection.mutable.Map

class MapCounter extends topwords.Counter {

  private val scores = scala.collection.mutable.Map[String, Int]()

  def account(word: String) : Int = {
    if (scores.contains(word)) scores(word) += 1  else scores(word) = 1
    getWordCount(word)
  }

  def getWords() : Map[String, Int] = {
    scores
  }

  def getWordCount(word: String) : Int = {
    scores.getOrElse(word, -1);
  }

  def getWordCount() : Int = {
    scores.size
  }
  // Method to increment the word count
  def increment(word: String): Unit = {
    scores.put(word, scores.getOrElse(word, 0) + 1)
  }
  
  // Decrement the word count and remove it if the count reaches 0
  def decrement(word: String): Unit = {
    if (scores.contains(word)) {
      scores(word) -= 1
      if (scores(word) <= 0) {
        scores.remove(word)
        ()
      }
    }
  }
}