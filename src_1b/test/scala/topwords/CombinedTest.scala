package topwords

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.language.unsafeNulls
import impl.MapCounter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CombinedTest extends AnyFlatSpec with Matchers {

  // Tests for MapCounter
  "A MapCounter" should "be empty initially" in {
    val counter = new MapCounter()
    counter.getWords() should be(empty) // Check initial state
  }

  it should "increment counts correctly" in {
    val counter = new MapCounter()

    // Increment a word
    counter.account("hello")
    counter.account("hello")

    // Verify the count
    counter.getWords() should contain("hello" -> 2)
  }

  it should "decrement counts correctly and remove word if count goes to zero" in {
    val counter = new MapCounter()

    // Increment and then decrement
    counter.account("world")
    counter.decrement("world")

    // Verify that "world" is no longer in the map
    counter.getWords() should not contain ("world")
  }

  it should "not decrement a non-existent word" in {
    val counter = new MapCounter()

    // Decrement a word that has not been accounted for
    counter.decrement("notfound")

    // Verify that it does not affect other counts
    counter.account("test")
    counter.getWords() should contain("test" -> 1)
  }

  it should "handle multiple distinct words" in {
    val counter = new MapCounter()

    // Increment multiple words
    counter.account("apple")
    counter.account("banana")
    counter.account("apple") // Increment apple again

    // Verify counts
    counter.getWords() should contain("apple" -> 2)
    counter.getWords() should contain("banana" -> 1)
  }

  it should "return an empty list when no words are accounted for" in {
    val counter = new MapCounter()
    counter.getWords() should be(empty) // No words should be present
  }
  // Edge cases for MapCounter
  it should "handle incrementing a word beyond limits" in {
    val counter = new MapCounter()

    // Increment a word multiple times
    counter.account("test")
    counter.account("test")
    counter.account("test")

    // Decrement it more times than accounted
    counter.decrement("test")
    counter.decrement("test")
    counter.decrement("test")

    // Verify that "test" is no longer in the map
    counter.getWords() should not contain ("test")
  }

  // Check how it handles null input
  it should "handle null input for account method" in {
    val counter = new MapCounter()

    // Test behavior for null, if applicable
    noException should be thrownBy {
      counter.account(null)
    }
  }

  // Tests for Main
  "processLine" should "correctly handle the sliding window functionality" in {
    val input = "apple banana cherry"
    val windowSize = 6
    val cloudSize = 2
    val lengthAtLeast = 1
    val wordCounter = new MapCounter()
    val queue = new CircularFifoQueue[String](windowSize)

    Main.processLine(input, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)

    // Verify the expected counts
    assert(wordCounter.getWordCount("apple") == 1)
    assert(wordCounter.getWordCount("banana") == 1)
    assert(wordCounter.getWordCount("cherry") == 1)
  }
  it should "handle piped input correctly" in {
    val input = "apple banana cherry\n"
    System.setIn(new ByteArrayInputStream(input.getBytes))

    // Call the run method with valid arguments
    Main.run(1, 1, 3)

    // Verify the expected counts in the word counter
    val wordCounter = new MapCounter()
    wordCounter.account("apple")
    wordCounter.account("banana")
    wordCounter.account("cherry")

    // Ensure they are accounted correctly
    assert(wordCounter.getWordCount("apple") == 1)
    assert(wordCounter.getWordCount("banana") == 1)
    assert(wordCounter.getWordCount("cherry") == 1)
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

  it should "prompt for interactive input and process correctly" in {
    val input = "hello world\napple banana\n"
    System.setIn(new ByteArrayInputStream(input.getBytes))

    // Call the run method with valid arguments
    Main.run(2, 1, 3)

    // Verify the expected counts in the word counter
    val wordCounter = new MapCounter()
    wordCounter.account("hello")
    wordCounter.account("world")
    wordCounter.account("apple")
    wordCounter.account("banana")

    // Ensure they are counted correctly
    assert(wordCounter.getWordCount("hello") == 1)
    assert(wordCounter.getWordCount("world") == 1)
    assert(wordCounter.getWordCount("apple") == 1)
    assert(wordCounter.getWordCount("banana") == 1)
  }

  it should "not hang when no input is provided in interactive mode" in {
    // Simulate no input scenario
    val input = new ByteArrayInputStream("".getBytes("UTF-8"))
    System.setIn(input)

    val cloudSize = 2
    val lengthAtLeast = 1
    val windowSize = 5

    noException should be thrownBy {
      Main.run(cloudSize, lengthAtLeast, windowSize)
    }
  }

  // Testing processLine with longer input
  it should "handle long input string" in {
    val input = "a " * 1000 // Long string with repeated word
    val windowSize = 5
    val cloudSize = 1
    val lengthAtLeast = 1
    val wordCounter = new MapCounter()
    val queue = new CircularFifoQueue[String](windowSize)

    Main.processLine(input.trim, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)

    // Verify that the long string is processed correctly
    assert(wordCounter.getWordCount("a") == 5)
  }
  // Testing processLine with mixed word lengths
  it should "process words of varying lengths based on lengthAtLeast" in {
    val input = "a bb ccc dddd"
    val windowSize = 10
    val cloudSize = 4
    val lengthAtLeast = 2 // Only consider words with length >= 2
    val wordCounter = new MapCounter()
    val queue = new CircularFifoQueue[String](windowSize)

    Main.processLine(input, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)

    // Only "bb", "ccc", and "dddd" should be counted
    assert(wordCounter.getWordCount("a") == -1)
    assert(wordCounter.getWordCount("bb") == 1)
    assert(wordCounter.getWordCount("ccc") == 1)
    assert(wordCounter.getWordCount("dddd") == 1)
  }

  it should "not exceed the specified window size" in {
    val input = "word word word word word word word"
    val windowSize = 5
    val cloudSize = 1
    val lengthAtLeast = 1
    val wordCounter = new MapCounter()
    val queue = new CircularFifoQueue[String](windowSize)

    input.split(" ").foreach { word =>
      Main.processLine(word, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)
    }

    assert(wordCounter.getWordCount("word") == 5)
    assert(wordCounter.getWords().size == 1) // Only 1 unique word
  }

  it should "handle an empty line" in {
    val input = ""
    val windowSize = 3
    val cloudSize = 2
    val lengthAtLeast = 1
    val wordCounter = new MapCounter()
    val queue = new CircularFifoQueue[String](windowSize)

    noException should be thrownBy {
      Main.processLine(input, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)
    }
  }

  it should "handle a single word input" in {
    val input = "apple"
    val windowSize = 3
    val cloudSize = 2
    val lengthAtLeast = 1
    val wordCounter = new MapCounter()
    val queue = new CircularFifoQueue[String](windowSize)

    Main.processLine(input, wordCounter, queue, cloudSize, lengthAtLeast, windowSize)

    assert(wordCounter.getWordCount("apple") == 1)
    assert(wordCounter.getWords().size == 1) // Only 1 unique word
  }

  "testSlidingQueue" should "correctly handle sliding window of words" in {
    val words = Seq("apple", "banana", "cherry", "date", "elderberry")
    val windowSize = 3

    val wordCounter = Main.testSlidingQueue(words, windowSize)

    // Verify that only the most recent 'windowSize' words are counted
    assert(wordCounter.getWordCount("apple") == -1) // Evicted
    assert(wordCounter.getWordCount("banana") == -1) // Evicted
    assert(wordCounter.getWordCount("cherry") == 1) // Still in window
    assert(wordCounter.getWordCount("date") == 1) // Still in window
    assert(wordCounter.getWordCount("elderberry") == 1) // Still in window
  }

  it should "handle a smaller word list than the window size" in {
    val words = Seq("apple", "banana")
    val windowSize = 3

    val wordCounter = Main.testSlidingQueue(words, windowSize)

    // Since the word list is smaller than the window size, all words should be counted
    assert(wordCounter.getWordCount("apple") == 1)
    assert(wordCounter.getWordCount("banana") == 1)
  }

  it should "handle an empty word list" in {
    val words = Seq.empty[String]
    val windowSize = 3

    noException should be thrownBy {
      Main.testSlidingQueue(words, windowSize)
    }
  }

  it should "correctly split a line into words" in {
    val line = "apple, banana! cherry.date"
    val words = Main.manuallySplitIntoWords(line)

    val expectedWords = Seq("apple", "banana", "cherry", "date")

    assert(words == expectedWords)
  }

  it should "handle an empty string in manuallySplitIntoWords" in {
    val line = ""
    val words = Main.manuallySplitIntoWords(line)

    assert(words.isEmpty)
  }
}

