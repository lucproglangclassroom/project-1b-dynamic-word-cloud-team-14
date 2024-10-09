
package topwords

import impl.MapCounter
import org.apache.commons.collections4.queue.CircularFifoQueue

trait InputProcessor {
  def processLine(line: String, wordCounter: MapCounter, queue: QueueManager,
                  cloudSize: Int, lengthAtLeast: Int, windowSize: Int): Unit

  def manuallySplitIntoWords(line: String): Seq[String]
  
  def printWordCloud(counter: MapCounter, cloudSize: Int): Unit
  
}
