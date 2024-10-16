
package topwords

import impl.{MapCounter, QueueManagerImpl}
import org.apache.commons.collections4.queue.CircularFifoQueue

trait InputProcessor {
  def processLine(line: String, wordCounter: MapCounter, queue: QueueManagerImpl,
                  cloudSize: Int, lengthAtLeast: Int, windowSize: Int): (MapCounter, QueueManagerImpl)

  def manuallySplitIntoWords(line: String): Seq[String]
  
  def printWordCloud(counter: MapCounter, cloudSize: Int): Unit
  
}
