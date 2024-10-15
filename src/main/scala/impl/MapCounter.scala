
package impl

import org.log4s._
import scala.collection.immutable.Map

/** mapcounter originally uses mutable map, change so it runs
 in a purely functional way that does not use any mutable state **/

/** basically get rid of any counter variable. update to use immutable.map
instead of muttable.Map**/

/** change account, increment, and decrement as they all use counter
variables **/

// change to use immuatble.Map

// counter trait for account, increment, and decrement

class MapCounter(scores: Map[String, Int] = Map.empty) extends topwords.Counter {

  private val logger = getLogger("Main")

  // def account is mutable, change so it does not use any mutable state.
  override def account(word: String): (MapCounter, Int) = {
    
    // account for a word
    // update count if word is present or add if not present
    val newScores = scores.updatedWith(word) {
      
      // up the count by 1
      case Some(count) => Some(count+1) 
      // add word with count 1 if doesnt exist
      case None => Some(1)
    }

    logger.debug(s"Updated count for is: $word, new count: ${newScores(word)}")
    (new MapCounter(newScores), newScores(word))
  }


  def getWords(): Map[String, Int] = {
    logger.debug(s"Returning all words with counts: ${scores.mkString(", ")}")
    scores
  }


  def getWordCount(word: String): Int = {
    val count = scores.getOrElse(word, -1)
    logger.debug(s"Retrieved count for word: $word, count: $count")
    count
  }


  def getWordCount(): Int = {
    val size = scores.size
    logger.debug(s"Total distinct word count: $size")
    size
  }

  // increment count for word using immutable map
  override def increment(word: String): topwords.Counter = {

    // update
    val newScores = scores.updatedWith(word) {
      // up the count by 1
      case Some(count) => Some(count+1)
      // add word if it doesnt exist
      case None => Some(1)
    }

    logger.debug(s"Incremented word is: $word, new count: ${newScores(word)}")
    
    new MapCounter(newScores)

  }

  // update to make immutable
  // decrease count for word
  override def decrement(word: String): topwords.Counter = {

    //update
    val newScores = scores.updatedWith(word) {

      // decrease count by 1
      case Some(count) if count > 1 => Some(count-1)
      // remove word if count doesnt exist
      case _ => None
    }

    logger.debug(s"Decremented count for word is: $word, new count: ${newScores.getOrElse(word, 0)}")

    new MapCounter(newScores)
  }
}
