package topwords

import scala.collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Suite
import org.scalatest.matchers.must.Matchers.*

import scala.language.postfixOps

class topwordsTest extends AnyFlatSpec with Suite:
  
  test("iterator should correctly iterate through lines in testFile.txt and store as an array") {

    val filePath = Paths.get("doc", "lestimes10-1.txt").toFile 
    val source = Source.fromFile(filePath)
    val inputStream = new ByteArrayInputStream(source.getBytes)
    
    val iteratorClass = new IteratorClass(inputStream)
    val result = iteratorClass.getLines
    
    //Assert
    assert(result.sameElements(source.toArray))


    source.close()
  }
  test("Counter should correctly count occurrences of each unique word in the array") {

    val words = Array("apple", "banana", "apple", "orange", "banana", "apple")
    
    val counter = new Counter(words)
    val result = counter.getWordCount

    // Assert
    assert(result == Map("apple" -> 3, "banana" -> 2, "orange" -> 1))
  }

  test("Counter should handle an empty array") {

    val emptyArray = Array.empty[String]


    val counter = new Counter(emptyArray)
    val result = counter.getWordCount

    // Assert
    assert(result.isEmpty)
  }

  test("CounterClass should handle an array with one word") {

    val singleWordArray = Array("apple")


    val counter = new Counter(singleWordArray)
    val result = counter.getWordCount

    // Assert
    assert(result == Map("apple" -> 1))
  }

  
end topwordsTest
