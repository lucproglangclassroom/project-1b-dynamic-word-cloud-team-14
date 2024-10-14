package topwords

import scala.util.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import impl.{InputProcessorImpl, MapCounter, QueueManagerImpl}
import java.io.ByteArrayInputStream
import scala.language.unsafeNulls

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
    val updatedCounter = counter.account("world").asInstanceOf[MapCounter]
    val finalCounter = updatedCounter.decrement("world").asInstanceOf[MapCounter]

    // Verify that "world" is no longer in the map
    finalCounter.getWords() should not contain "world"
  }

  it should "not decrement a non-existent word" in {
    val counter = new MapCounter()

    // Decrement a word that has not been accounted for
    val updatedCounter = counter.decrement("notfound").asInstanceOf[MapCounter]

    // Verify that it does not affect other counts
    val furtherUpdatedCounter = updatedCounter.account("test")._1.asInstanceOf[MapCounter]
    furtherUpdatedCounter.getWords() should contain("test" -> 1)
  }

  it should "handle multiple distinct words" in {
    val counter = new MapCounter()

    // Increment multiple words
    val updatedCounter = counter.account("apple").asInstanceOf[MapCounter]
    val updatedCounter2 = updatedCounter.account("banana").asInstanceOf[MapCounter]

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
    val processor = new InputProcessorImpl()
    val mockQueue = mock[QueueManagerImpl]
    val mockMapCounter = mock[MapCounter]
    val line = "Scala is amazing"
    val cloudSize = 10
    val lengthAtLeast = 3
    val windowSize = 5

    when(mockQueue.isFull()).thenReturn(false)

    processor.processLine(line, mockMapCounter, mockQueue, cloudSize, lengthAtLeast, windowSize)

    verify(mockQueue).addWord("Scala")
    verify(mockQueue).addWord("amazing")
    verify(mockMapCounter).account("Scala")
    verify(mockMapCounter).account("amazing")
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

    when(mockQueue.isFull()).thenReturn(true)
    when(mockQueue.evictOldest()).thenReturn(Some("oldestWord") -> 1)

    processor.processLine(line, mockMapCounter, mockQueue, cloudSize, lengthAtLeast, windowSize)

    verify(mockQueue).evictOldest()
    verify(mockMapCounter).decrement("oldestWord")
  }

  it should "skip short words" in {
    val processor = new InputProcessorImpl()
    val mockQueue = mock[QueueManagerImpl]
    val mockMapCounter = mock[MapCounter]
    val line = "A is example"
    val cloudSize = 10
    val lengthAtLeast = 3
    val windowSize = 5

    processor.processLine(line, mockMapCounter, mockQueue, cloudSize, lengthAtLeast, windowSize)

    verify(mockQueue).addWord("example")
    verify(mockMapCounter).account("example")
    verify(mockQueue, never()).addWord("A")
    verify(mockQueue, never()).addWord("is")
  }

  "printWordCloud" should "print a sorted word cloud" in {
    val processor = new InputProcessorImpl()
    val mockCounter = mock[MapCounter]
    when(mockCounter.getWords()).thenReturn(Map("Scala" -> 5, "Code" -> 3, "Fun" -> 7))

    // Capture output using Console.withOut
    val outStream = new java.io.ByteArrayOutputStream()
    Using.resource(new java.io.PrintStream(outStream)) { printStream =>
      Console.withOut(printStream) {
        processor.printWordCloud(mockCounter, 3)
      }

      // Verify output as needed (can be customized based on implementation)
      val output = outStream.toString
      output should include("Scala")
      output should include("Code")
      output should include("Fun")
    }
  }

  it should "handle invalid input arguments gracefully" in {
    val caught = intercept[IllegalArgumentException] {
      Main.run(0, 1, 1) // Invalid cloudSize
    }
    assert(caught.getMessage == "All arguments must be positive numbers.")

    val caught2 = intercept[IllegalArgumentException] {
      Main.run(1, 0, 1) // Invalid lengthAtLeast
    }
    assert(caught2.getMessage == "All arguments must be positive numbers.")

    val caught3 = intercept[IllegalArgumentException] {
      Main.run(1, 1, 0) // Invalid windowSize
    }
    assert(caught3.getMessage == "All arguments must be positive numbers.")
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

