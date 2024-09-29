
package topwords

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class MainTest extends AnyFlatSpec with Matchers {

  "processInput" should "correctly handle the sliding window functionality" in {
    val input = Seq("apple banana cherry", "", "date elderberry")
    val windowSize = 6
    val cloudSize = 2
    val lengthAtLeast = 1

    val wordCounter = Main.processInput(input, cloudSize, lengthAtLeast, windowSize)

    // Verify the expected counts
    assert(wordCounter.getWordCount("apple") == 1)
    assert(wordCounter.getWordCount("banana") == 1)
    assert(wordCounter.getWordCount("cherry") == 1)
    assert(wordCounter.getWordCount("date") == 1)
    assert(wordCounter.getWordCount("elderberry") == 1)
  }

  it should "not exceed the specified window size" in {
    val input = Seq.fill(10)("word")
    val windowSize = 5
    val cloudSize = 1
    val lengthAtLeast = 1

    val wordCounter = Main.processInput(input, cloudSize, lengthAtLeast, windowSize)

    assert(wordCounter.getWordCount("word") == 5)
    assert(wordCounter.getWords().size == 1) // Only 1 unique word
  }

  it should "handle an empty word list" in {
    val input = Seq.empty[String]
    val windowSize = 3
    val cloudSize = 2
    val lengthAtLeast = 1

    noException should be thrownBy {
      Main.processInput(input, cloudSize, lengthAtLeast, windowSize)
    }
  }

  it should "handle a single word input" in {
    val input = Seq("apple")
    val windowSize = 3
    val cloudSize = 2
    val lengthAtLeast = 1

    val wordCounter = Main.processInput(input, cloudSize, lengthAtLeast, windowSize)

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
