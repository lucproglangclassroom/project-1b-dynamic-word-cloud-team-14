package topwords

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import impl.MapCounter

class MapCounterTest extends AnyFlatSpec with Matchers {

  "A MapCounter" should "increment counts correctly" in {
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
}

