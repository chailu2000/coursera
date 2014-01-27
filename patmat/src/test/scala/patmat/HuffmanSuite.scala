package patmat

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import patmat.Huffman._

@RunWith(classOf[JUnitRunner])
class HuffmanSuite extends FunSuite {
  trait TestTrees {
    val t1 = Fork(Leaf('a', 2), Leaf('b', 3), List('a', 'b'), 5)
    val t2 = Fork(Fork(Leaf('a', 2), Leaf('b', 3), List('a', 'b'), 5), Leaf('d', 4), List('a', 'b', 'd'), 9)
  }

  test("weight of a larger tree") {
    new TestTrees {
      assert(weight(t1) === 5)
    }
  }

  test("chars of a larger tree") {
    new TestTrees {
      assert(chars(t2) === List('a', 'b', 'd'))
    }
  }

  test("string2chars(\"hello, world\")") {
    assert(string2Chars("hello, world") === List('h', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd'))
  }

  test("times of a char list") {
    new TestTrees {
      assert(times(string2Chars("abaaabcd")).contains(('c', 1)))
      assert(times(string2Chars("abaaabcd")).contains(('d', 1)))
      assert(times(string2Chars("abaaabcd")).contains(('b', 2)))
      assert(times(string2Chars("abaaabcd")).contains(('a', 4)))
    }
  }

  test("makeOrderedLeafList for some frequency table") {
    assert(makeOrderedLeafList(List(('t', 2), ('e', 1), ('x', 3))) === List(Leaf('e', 1), Leaf('t', 2), Leaf('x', 3)))
  }

  test("combine of some leaf list") {
    val leaflist = List(Leaf('e', 1), Leaf('t', 2), Leaf('x', 4))
    assert(combine(leaflist) === List(Fork(Leaf('e', 1), Leaf('t', 2), List('e', 't'), 3), Leaf('x', 4)))
  }

  test("combine of leaf and fork") {
    val leaflist = List(Leaf('a', 1), Fork(Leaf('e', 3), Leaf('t', 4), List('e', 't'), 7), Leaf('x', 8))
    assert(combine(leaflist) === List(Fork(Leaf('a', 1), Fork(Leaf('e', 3), Leaf('t', 4), List('e', 't'), 7), List('a', 'e', 't'), 8), Leaf('x', 8)))
  }

  test("until should produce a list of one CodeTree") {
    val leaflist = List(Leaf('e', 1), Leaf('t', 2), Leaf('x', 3), Leaf('z', 4))
    val untilList = until(singleton, combine)(leaflist)
    assert(untilList.size === 1)
    assert(untilList === List(Fork(Leaf('z', 4), Fork(Fork(Leaf('e', 1), Leaf('t', 2), List('e', 't'), 3), Leaf('x', 3), List('e', 't', 'x'), 6), List('z', 'e', 't', 'x'), 10)))
  }

  test("createCodeTree should procude a codeTree") {
    val chars = string2Chars("ztexxtzxzz");
    assert(createCodeTree(chars) === Fork(Leaf('z', 4), Fork(Fork(Leaf('e', 1), Leaf('t', 2), List('e', 't'), 3), Leaf('x', 3), List('e', 't', 'x'), 6), List('z', 'e', 't', 'x'), 10))
  }

  test("decode") {
    new TestTrees {
      assert(decode(t1, List(0, 1)) === List('a', 'b'))
      assert(decode(t2, List(0, 0, 1, 0, 1, 1)) === List('a', 'd', 'b', 'd'))
    }
  }

  test("encode") {
    new TestTrees {
      assert(encode(t1)(List('a', 'b')) === List(0, 1))
      assert(encode(t2)(List('a', 'd', 'b', 'd')) === List(0, 0, 1, 0, 1, 1))
    }
  }

  test("decode and encode a very short text should be identity") {
    new TestTrees {
      assert(decode(t1, encode(t1)("ab".toList)) === "ab".toList)
      assert(decode(t2, encode(t2)("dabbad".toList)) === "dabbad".toList)
    }
  }

  test("convert") {
    new TestTrees {
      assert(convert(t1) === List(('a', List(0)), ('b', List(1))))
      assert(convert(t2) === List(('a', List(0, 0)), ('b', List(0, 1)), ('d', List(1))))
    }
  }

  ignore("merge code tables") {
    val merged = mergeCodeTables(List(('a', List(0)), ('b', List(0)), ('c', List(0))), List(('a', List(0)), ('b', List(1)), ('d', List(1))))
    assert(merged.contains(('a', List(0, 0))))
    assert(merged.contains(('b', List(0, 1))))
    assert(merged.contains(('c', List(0))))
    assert(merged.contains(('d', List(1))))
  }

  test("quickEncode") {
    assert(quickEncode(frenchCode)(decode(frenchCode, secret)) === secret)
  }
}