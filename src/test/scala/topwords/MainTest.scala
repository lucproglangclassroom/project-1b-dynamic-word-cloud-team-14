
package topwords

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MainTest extends AnyFlatSpec with Matchers {

  "testSlidingQueue" should "correctly handle the sliding window functionality" in {
    val words = Seq("apple", "banana", "cherry", "date", "elderberry")
    val windowSize = 3

    // Call the testSlidingQueue method
    val counter = Main.testSlidingQueue(words, windowSize)

    // Verify the expected counts
    val appleCount = counter.getWordCount("apple")
    val bananaCount = counter.getWordCount("banana")
    val cherryCount = counter.getWordCount("cherry")
    val dateCount = counter.getWordCount("date")
    val elderberryCount = counter.getWordCount("elderberry")

    // Assertions
    assert(appleCount == 1)
    assert(bananaCount == 1)
    assert(cherryCount == 1)
    assert(dateCount == 1)
    assert(elderberryCount == -1) // "elderberry" should have been evicted

    // Check the size of the MapCounter
    val uniqueWordsCount = counter.getWords().size
    assert(uniqueWordsCount == 4) // Only 4 unique words should remain
  }

  it should "not exceed the specified window size" in {
    val words = Seq.fill(10)("word")
    val windowSize = 5

    // Call the testSlidingQueue method
    val counter = Main.testSlidingQueue(words, windowSize)

    // Check that the count for "word" is 5
    assert(counter.getWordCount("word") == 5)

    // Ensure the MapCounter only retains the expected number of unique words
    assert(counter.getWords().size == 1) // Only 1 unique word
  }

  it should "handle an empty word list" in {
    val words = Seq.empty[String]
    val windowSize = 3

    // Call the testSlidingQueue method
    val counter = Main.testSlidingQueue(words, windowSize)

    // Ensure no words are counted
    assert(counter.getWords().isEmpty) // No words should be present
  }

  it should "handle a window size of 1 correctly" in {
    val words = Seq("word1", "word2", "word3")
    val windowSize = 1

    // Call the testSlidingQueue method
    val counter = Main.testSlidingQueue(words, windowSize)

    // Only the last word should be counted
    assert(counter.getWordCount("word1") == -1)
    assert(counter.getWordCount("word2") == -1)
    assert(counter.getWordCount("word3") == 1)

    // Check that only 1 unique word remains in the counter
    assert(counter.getWords().size == 1) // Only 1 unique word
  }
}


