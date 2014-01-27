package funsets

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * This class is a test suite for the methods in object FunSets. To run
 * the test suite, you can either:
 *  - run the "test" command in the SBT console
 *  - right-click the file in eclipse and chose "Run As" - "JUnit Test"
 */
@RunWith(classOf[JUnitRunner])
class FunSetSuite extends FunSuite {


  /**
   * Link to the scaladoc - very clear and detailed tutorial of FunSuite
   *
   * http://doc.scalatest.org/1.9.1/index.html#org.scalatest.FunSuite
   *
   * Operators
   *  - test
   *  - ignore
   *  - pending
   */

  /**
   * Tests are written using the "test" operator and the "assert" method.
   */
  test("string take") {
    val message = "hello, world"
    assert(message.take(5) == "hello")
  }

  /**
   * For ScalaTest tests, there exists a special equality operator "===" that
   * can be used inside "assert". If the assertion fails, the two values will
   * be printed in the error message. Otherwise, when using "==", the test
   * error message will only say "assertion failed", without showing the values.
   *
   * Try it out! Change the values so that the assertion fails, and look at the
   * error message.
   */
  test("adding ints") {
    assert(1 + 2 === 3)
  }

  
  import FunSets._

  test("contains is implemented") {
    assert(contains(x => true, 100))
  }
  
  /**
   * When writing tests, one would often like to re-use certain values for multiple
   * tests. For instance, we would like to create an Int-set and have multiple test
   * about it.
   * 
   * Instead of copy-pasting the code for creating the set into every test, we can
   * store it in the test class using a val:
   * 
   *   val s1 = singletonSet(1)
   * 
   * However, what happens if the method "singletonSet" has a bug and crashes? Then
   * the test methods are not even executed, because creating an instance of the
   * test class fails!
   * 
   * Therefore, we put the shared values into a separate trait (traits are like
   * abstract classes), and create an instance inside each test method.
   * 
   */

  trait TestSets {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val s3 = singletonSet(3)
  }

  /**
   * This test is currently disabled (by using "ignore") because the method
   * "singletonSet" is not yet implemented and the test would fail.
   * 
   * Once you finish your implementation of "singletonSet", exchange the
   * function "ignore" by "test".
   */
  test("singletonSet(1) contains 1") {
    
    /**
     * We create a new instance of the "TestSets" trait, this gives us access
     * to the values "s1" to "s3". 
     */
    new TestSets {
      /**
       * The string argument of "assert" is a message that is printed in case
       * the test fails. This helps identifying which assertion failed.
       */
      assert(contains(s1, 1), "Singleton")
    }
  }

  test("union contains all elements") {
    new TestSets {
      val s = union(s1, s2)
      assert(contains(s, 1), "Union 1")
      assert(contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
    }
  }
  
  test("singletonSet(1) does not contain 2") {
    new TestSets {
      assert(!contains(s1, 2), "Singleton not")
    }
  }
  
  test("interset contains only elements in both sets") {
    new TestSets {
      val s = union(s1, s2)
      val t = union(s2, s3)
      val inter = intersect(s,t)
      
      assert(contains(inter, 2), "Inter 1")
      assert(!contains(inter, 1), "Inter 2")
      assert(!contains(inter, 3), "Inter 3")
      
    }
  }
  test("diff contains only elements in s but not t") {
    new TestSets {
      val s = union(union(s1, s2), s3)
      val d = diff(s,s2)
      
      assert(contains(d, 1), "Diff 1")
      assert(contains(d, 3), "Diff 2")
      assert(!contains(d, 2), "Diff 3")
      
    }
  }
  test("filter contains all elements in s that satisfy p") {
    new TestSets {
      val s = union(union(s1, s2), s3)
      val f = filter(s, elem => elem%2 == 0)
      
      assert(contains(f, 2), "Filter 1")
      assert(!contains(f, 1), "Filter 2")
      assert(!contains(f, 3), "Filter 3")
      
    }
  }
  test("forall returns true if all bounded elements in s satisfy p") {
    new TestSets {
      val s = union(union(union(s1, s2), s3), singletonSet(2000))
      val f = forall(s, elem => elem%2 == 0)
      val g = forall(s, elem => elem<=3 && elem>=1)
      
      assert(!f, "Forall 1")
      assert(g, "Forall 2")
    }
  }
  test("exists returns true if any bounded element in s satisfy p") {
    new TestSets {
      val s = union(union(union(s1, s2), s3), singletonSet(2000))
      val f = exists(s, elem => elem%2 == 0)
      val g = exists(s, elem => elem<=3 && elem>=1)
      val h = exists(s, elem => elem%10 == 0)
      
      assert(f, "Exists 1")
      assert(g, "Exists 2")
      assert(!h, "Exists 3")
    }
  }
  test("map contains all elements after applied f") {
    new TestSets {
      val s = union(union(s1, s2), s3)
      val multiply4 = map(s, elem => elem*4)
      val add2 = map(s, elem => elem+2)
      val neg = map(s, elem => -elem)
      
      assert(contains(multiply4,12), "Map 1")
      assert(contains(multiply4,8), "Map 1.1")
      assert(contains(multiply4,4), "Map 1.2")
      assert(!contains(multiply4,1), "Map 1.3")
      assert(!contains(multiply4,2), "Map 1.4")
      assert(!contains(multiply4,3), "Map 1.5")
      assert(contains(add2,5), "Map 2")
      assert(!contains(neg,2), "Map 3")
    }
  }  
}
