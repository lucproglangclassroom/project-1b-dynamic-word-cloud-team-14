package topwords

import scala.util.Using
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import impl.{InputProcessorImpl, MapCounter, QueueManagerImpl}
import java.io.ByteArrayInputStream

class NewTest extends AnyFlatSpec with Matchers with MockitoSugar {

  // Tests for MapCounter
  "A MapCounter" should "be empty initially" in {
    val counter = new MapCounter()
    counter.getWords() should be(empty) // Check initial state
  }

  it should "increment counts correctly" in {
    val counter = new MapCounter(Map.empty[String, Int])

    val (updatedCounter, countAfterFirst) = counter.account("hello")
    countAfterFirst should be(1)

    val (finalCounter, countAfterSecond) = updatedCounter.account("hello")
    countAfterSecond should be(2)
  }

  it should "decrement counts correctly and remove word if count goes to zero" in {
    val counter = new MapCounter()

    // Increment the count for "world"
    val (updatedCounter, _) = counter.account("world")
    val finalCounter = updatedCounter.decrement("world")

    // Verify that "world" is no longer in the map
    finalCounter.getWords() should not contain "world"
  }

  it should "not decrement a non-existent word" in {
    val counter = new MapCounter()

    // Decrement a word that has not been accounted for
    val updatedCounter = counter.decrement("notfound")

    // Verify that it does not affect other counts
    val furtherUpdatedCounter = updatedCounter.account("test")._1
    furtherUpdatedCounter.getWords() should contain("test" -> 1)
  }

  it should "handle multiple distinct words" in {
    val counter = new MapCounter()

    // Increment multiple words
    val (updatedCounter, _) = counter.account("apple")
    val (updatedCounter2, _) = updatedCounter.account("banana")

    // Verify counts
    updatedCounter2.getWords() should contain("apple" -> 1)
    updatedCounter2.getWords() should contain("banana" -> 1)
  }

  it should "return an empty list when no words are accounted for" in {
    val counter = new MapCounter()
    counter.getWords() should be(empty) // No words should be present
  }

  // Tests for InputProcessorImpl
  "InputProcessorImpl" should "correctly split lines into words" in {
    val processor = new InputProcessorImpl()
    val line = "apple, banana! cherry.date"
    val words = processor.manuallySplitIntoWords(line)

    val expectedWords = Seq("apple", "banana", "cherry", "date")
    words shouldEqual expectedWords
  }

  it should "handle an empty string in manuallySplitIntoWords" in {
    val processor = new InputProcessorImpl()
    val words = processor.manuallySplitIntoWords("")

    words shouldBe empty
  }

  it should "process a line with valid words" in {
    val cloudSize = 5
    val lengthAtLeast = 3
    val windowSize = 5

    // Mock instances for MapCounter and QueueManagerImpl
    val mockWordCounter = mock[MapCounter]
    val queueManager = new QueueManagerImpl(windowSize)
    val inputProcessor = new InputProcessorImpl()

    // Define the input line
    val line = "Scala is amazing"

    // Set up mock behavior for MapCounter
    when(mockWordCounter.account("Scala")).thenReturn((mockWordCounter, 1))
    when(mockWordCounter.account("is")).thenReturn((mockWordCounter, 0))
    when(mockWordCounter.account("amazing")).thenReturn((mockWordCounter, 1))

    // Simulate processing the input line directly
    val (updatedWordCounter, updatedQueueManager) = inputProcessor.processLine(
      line,
      mockWordCounter,
      queueManager,
      cloudSize,
      lengthAtLeast,
      windowSize
    )

    // Verify the state of the queue after processing the line
    updatedQueueManager.queue should contain("Scala")
    updatedQueueManager.queue shouldNot contain("is")
    updatedQueueManager.queue should contain("amazing")
  }


  it should "skip processing an empty line" in {
    val processor = new InputProcessorImpl()
    val mockQueue = mock[QueueManagerImpl]
    val mockMapCounter = mock[MapCounter]
    val line = ""
    val cloudSize = 10
    val lengthAtLeast = 3
    val windowSize = 5

    processor.processLine(line, mockMapCounter, mockQueue, cloudSize, lengthAtLeast, windowSize)

    verifyNoInteractions(mockQueue)
    verifyNoInteractions(mockMapCounter)
  }

  it should "evict the oldest word when the queue is full" in {
    val processor = new InputProcessorImpl()
    val mockQueue = mock[QueueManagerImpl]
    val mockMapCounter = mock[MapCounter]
    val line = "Full queue example"
    val cloudSize = 10
    val lengthAtLeast = 3
    val windowSize = 5

    // Mock behavior for the queue
    when(mockQueue.isFull()).thenReturn(true)

    // Create a new instance of QueueManagerImpl for the evicted queue
    val newQueueAfterEviction = new QueueManagerImpl(windowSize)

    // Mock evictOldest to return the expected types
    when(mockQueue.evictOldest()).thenReturn(Some("oldestWord") -> newQueueAfterEviction)

    // Set up mock behavior for MapCounter
    when(mockMapCounter.decrement("oldestWord")).thenReturn(mockMapCounter)
    when(mockMapCounter.account("Full")).thenReturn((mockMapCounter, 0)) // Mock for "Full"
    when(mockMapCounter.account("queue")).thenReturn((mockMapCounter, 1)) // Mock for "queue"
    when(mockMapCounter.account("example")).thenReturn((mockMapCounter, 1)) // Mock for "example"

    // Simulate processing the input line
    val (updatedWordCounter, updatedQueueManager) = processor.processLine(
      line,
      mockMapCounter,
      mockQueue,
      cloudSize,
      lengthAtLeast,
      windowSize
    )

    // Verify that the oldest word is evicted and decremented
    verify(mockQueue).evictOldest()
    verify(mockMapCounter).decrement("oldestWord")

    // Ensure the updated word counter is the same mockMapCounter
    updatedWordCounter.getWordCount() should be(mockMapCounter.getWordCount())

  }

  it should "skip short words" in {
    val processor = new InputProcessorImpl()
    val mockQueue = mock[QueueManagerImpl]
    val mockMapCounter = mock[MapCounter]
    val line = "A is example"
    val cloudSize = 10
    val lengthAtLeast = 3
    val windowSize = 5

    // Mock the behavior of MapCounter to return expected tuples
    when(mockMapCounter.account("A")).thenReturn((mockMapCounter, 0)) // Short word
    when(mockMapCounter.account("is")).thenReturn((mockMapCounter, 0)) // Short word
    when(mockMapCounter.account("example")).thenReturn((mockMapCounter, 1)) // Valid word

    // Process the input line
    processor.processLine(line, mockMapCounter, mockQueue, cloudSize, lengthAtLeast, windowSize)

    // Verify that the correct words are added to the queue and accounted for
    verify(mockQueue).addWord("example")
    verify(mockMapCounter).account("example")
    verify(mockQueue, never()).addWord("A")
    verify(mockQueue, never()).addWord("is")
  }


  "Main" should "handle invalid input arguments gracefully" in {
    val caught = intercept[IllegalArgumentException] {
      Main.run(0, 1, 1) // Invalid cloudSize
    }
    caught.getMessage should be ("All arguments must be positive numbers.")

    val caught2 = intercept[IllegalArgumentException] {
      Main.run(1, 0, 1) // Invalid lengthAtLeast
    }
    caught2.getMessage should be ("All arguments must be positive numbers.")

    val caught3 = intercept[IllegalArgumentException] {
      Main.run(1, 1, 0) // Invalid windowSize
    }
    caught3.getMessage should be ("All arguments must be positive numbers.")
  }

  it should "not hang when no input is provided in interactive mode" in {
    // Simulate no input scenario
    val input = new ByteArrayInputStream("".getBytes("UTF-8"))
    System.setIn(input)

    noException should be thrownBy {
      Main.run(2, 1, 5)
    }
  }
}



