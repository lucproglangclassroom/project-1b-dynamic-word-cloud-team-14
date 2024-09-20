package topwords

// example straight from scalatest.org

import scala.collection.mutable.Stack

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Suite
import org.scalatest.matchers.must.Matchers.*

class StackSpec extends AnyFlatSpec with Suite:

  "A Stack" should "pop values in last-in-first-out order" in:
    val stack = Stack.empty[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() must equal(2)
    stack.pop() must equal(1)

  it should "throw NoSuchElementException if an empty stack is popped" in:
    val emptyStack = Stack.empty[Int]
    an[NoSuchElementException] must be thrownBy:
      emptyStack.pop()

end StackSpec
