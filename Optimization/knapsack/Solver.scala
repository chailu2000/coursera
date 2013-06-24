import scala.io.Source

object Solver {
  def main(args: Array[String]) = {
    solve(args)
  }
  def solve(args: Array[String]): Unit = {
    val fileName: String =
      if (args(0).startsWith("-file=")) {
        args(0).substring(6)
      } else { "" }

    val lines = Source.fromFile(fileName).getLines().toList
    //println(lines)
    val Array(is, capacity) = lines.head.split("\\s+")
    // raw values and weights, those that are greater than the capacity are filtered out
    val (vs, ws) = lines.tail.map(_.split("\\s+") match {
      case Array(x, y) => (Integer.parseInt(x), Integer.parseInt(y))
    }).unzip
    //.filter(e => e._2 <= Integer.parseInt(capacity))

    // filter out those weight that are larger than capacity
    val (wis, wis2) = ws.zipWithIndex.partition(_._1 <= Integer.parseInt(capacity))

    val ignoredIndices = wis2.unzip._2

    val weightsUnsorted = wis.unzip._1
    val valuesUnsorted = vs.zipWithIndex.filter(e => wis.unzip._2.contains(e._2)).unzip._1

    // sort weight at descending order
    val sortedWeights = weightsUnsorted.zipWithIndex.zip(valuesUnsorted).sortBy(_._1._1)(Ordering[Int].reverse)
    //println(sortedWeights)

    val weights = sortedWeights.unzip._1.unzip._1
    val values = sortedWeights.unzip._2

    val originalIndices = sortedWeights.unzip._1.unzip._2.toArray
    //println(values)
    //println(weights)
    println(is + " " + capacity)
    
    val items = values.size

    /**
     * Algorithms:
     * DP - Dynamic programming
     * BS - Best search
     */
    val algorithm = "BS"

    val (bestPair, bestPairReverse) =
      if (algorithm == "DP") {
        //println("Dynamic programming...")
        //println("Doing it normal order...")
        val bestPair = DP.dpAlgorithm(capacity, values, weights, items)
        //println("Doing it reverse order...")
        val bestPairReverse = DP.dpAlgorithm(capacity, values.reverse, weights.reverse, items)
        (bestPair, bestPairReverse)
      } else {
        //println("Best Search...")
        //println("Doing it normal order...")
        val bestPair = BS.bsAlgorithm(capacity, values, weights, items, ignoredIndices, originalIndices)
        // println("Doing it reverse order...")
        // val bestPairReverse = BS.bsAlgorithm(capacity, values.reverse, weights.reverse, items)
        (bestPair, (0, Array(0)))
      }

    /*
     * end of the algorithm
     */
    if (bestPair._1 >= bestPairReverse._1) {
      //println("Normal order yields better answer: ")
      printResult(bestPair)
    } else {
      println("Reverse order yields better answer: ")
      printResult((bestPairReverse._1, bestPairReverse._2.reverse))
    }
  }

  def printResult(bestPair: (Int, Array[Int])) = {
    // the required output format
    //println("Final answer in required format: ")
    println(bestPair._1 + " 0")
    println(bestPair._2.mkString(" "))
  }
}
object DP {
  def dynamicProgramming(values: List[Int], weights: List[Int], capacity: Int, items: Int, tArr: Array[Int], maxCapacity: Int, bestCase: Array[Int]): Int = {
    //println("Items: " + items + " capacity: " + capacity)
    if (items == 0 || capacity <= 0) {
      val r0 = 0
      //tArr(items) = 0
      //println("0 - " + r0 + " " + capacity + " " + items)
      r0
    } else {
      val dp = dynamicProgramming(values, weights, capacity, items - 1, tArr, maxCapacity, bestCase)
      if (weights(items - 1) <= capacity) {
        val dpval1 = dp
        val dpval2 = values(items - 1) + dynamicProgramming(values, weights, capacity - weights(items - 1), items - 1, tArr, maxCapacity, bestCase)
        if (dpval1 <= dpval2) {
          tArr(items - 1) = 1
          val r1 = dpval2
          //println("1 - " + r1 + " " + capacity + " " + items)
          r1
        } else {
          tArr(items - 1) = 0
          val r2 = dpval1
          //println("2 - " + r2 + " " + capacity + " " + items)
          //println("Not taking item " + items)
          r2
        }
      } else {
        val v = values.zipWithIndex.filter(e => (tArr(e._2) == 1)).foldLeft(0)(_ + _._1)
        val w = weights.zipWithIndex.filter(e => (tArr(e._2) == 1)).foldLeft(0)(_ + _._1)
        if (w <= maxCapacity) {
          //println(tArr.mkString(" ") + "=>" + v + "=>" + w)
          if (v >= bestCase(0)) {
            bestCase(0) = v
            for (i <- Range(0, tArr.size))
              bestCase(i + 1) = tArr(i)
          }
        }

        // Reset take array
        for (i <- Range(0, tArr.size))
          tArr(i) = 0
        val r3 = dp
        //println("3 - " + r3 + " " + capacity + " " + items)
        //println("Not taking item " + items)
        r3
      }
    }
  }

  def dpAlgorithm(capacity: String, values: List[Int], weights: List[Int], items: Int): (Int, Array[Int]) = {
    /*
       * algorithm goes here
       */
    val maxCapacity = Integer.parseInt(capacity)
    val takes = Array.fill(items)(0)
    val bestCase = Array.fill(items + 1)(0) // this is best value + corresponding takes
    val value = dynamicProgramming(values, weights, maxCapacity, items, takes, maxCapacity, bestCase)

    //println(value + " 0")
    //println(takes.mkString(" "))

    val bestValue = bestCase(0)
    val bestTake = bestCase.slice(1, bestCase.length)

    println("Best Value found: " + values.zipWithIndex.filter(e => (bestTake(e._2) == 1)).foldLeft(0)(_ + _._1))
    println("Total weights: " + weights.zipWithIndex.filter(e => (bestTake(e._2) == 1)).foldLeft(0)(_ + _._1))
    (bestValue, bestTake)
  }
}
object BS {
  def bestSearch(values: List[Int], weights: List[Int], maxCapacity: Int, node: Node): Node = {
    bestSearchArr(values, weights, maxCapacity, node :: List(), node)
  }
  def bestSearchArr(values: List[Int], weights: List[Int], maxCapacity: Int, nodeList: List[Node], bestNode: Node): Node = {
    if (nodeList.isEmpty) bestNode
    else {
      val node = nodeList.head
      //println("Node: " + node.toString)
      //println("Node List: " + nodeList)
      //println("Best Node: " + bestNode)
      if (node.estimate <= bestNode.value) {
        // ignore such node
        bestSearchArr(values, weights, maxCapacity, nodeList.tail, bestNode)
      } else {
        val idx = node.idx + 1
        val minWeight = weights.zipWithIndex.filter(e => !node.takes.contains(e._2)).unzip._1.min
        if (idx == values.size || node.room < minWeight) {
          // can't branch anymore
          // current node is a solution node, compare with bestNode so far.
          if (node.value >= bestNode.value) bestSearchArr(values, weights, maxCapacity, nodeList.tail, node)
          else bestSearchArr(values, weights, maxCapacity, nodeList.tail, bestNode)
        } else {
          // branch from the current node to the next level
          val valuesRest = values.drop(idx)
          val weightsRest = weights.drop(idx)
          val notTakeNode = new Node(node.value,
            node.room,
            node.value + estimate(valuesRest,
              weightsRest,
              node.room),
            idx,
            node.takes)

          if (node.room - weights(idx) >= 0) {
            val takeNode = new Node(values(idx) + node.value, node.room - weights(idx),
              values(idx) + node.value + estimate(valuesRest,
                weightsRest,
                node.room - weights(idx)),
              idx, idx :: node.takes)
            val nodeToInclude = if (takeNode.estimate >= notTakeNode.estimate) takeNode :: notTakeNode :: List() else notTakeNode :: List()
            if (takeNode.value >= bestNode.value) {
              bestSearchArr(values, weights, maxCapacity, (nodeToInclude ::: (nodeList.tail)), takeNode)
            } else {
              bestSearchArr(values, weights, maxCapacity, (nodeToInclude ::: (nodeList.tail)), bestNode)
            }
          } else {
        	if (bestNode.value >= notTakeNode.estimate) {
        		bestSearchArr(values, weights, maxCapacity, ( nodeList.tail), bestNode)
        	} else {
        		bestSearchArr(values, weights, maxCapacity, ((nodeList.tail)), bestNode)
        	}
          }
        }
      }
    }
  }
  def bsAlgorithm(capacity: String, values: List[Int], weights: List[Int], items: Int, ignoredIndices: List[Int], originalIndices: Array[Int]): (Int, Array[Int]) = {
    /*
       * algorithm goes here
       */
    val maxCapacity = Integer.parseInt(capacity)

    val estimateValue = estimate(values, weights, maxCapacity)
    println("initial estimate: " + estimateValue)
    //(0, Array(0))
    val rootNode = new Node(0, maxCapacity, estimateValue, -1, List());
    //val resultNode = rootNode

    val s = System.nanoTime
    val resultNode = bestSearch(values, weights, maxCapacity, rootNode)
    println("time: " + (System.nanoTime - s) / 1e9 + "s")

    //println(value + " 0")
    //println(takes.mkString(" "))

    val bestValue = resultNode.value
    val bestTake = Array.fill(items)(0)

    //println("Original indices: " + originalIndices.mkString(" "))
    for (i <- resultNode.takes) {
      bestTake(originalIndices(i)) = 1
    }

    //println(bestTake.mkString(" "))
    //println("Best Value found: " + values.zipWithIndex.filter(e => (bestTake(e._2) == 1)).foldLeft(0)(_ + _._1))
    //println("Total weights: " + weights.zipWithIndex.filter(e => (bestTake(e._2) == 1)).foldLeft(0)(_ + _._1))

    // add back ignored indices
    val bestTakeAll = Array.fill(items + ignoredIndices.size)(0)
    for (i <- bestTake.toList.zipWithIndex.filter(e => e._1 == 1).unzip._2) {
      bestTakeAll(i + ignoredIndices.filter(_ <= i).size) = 1
    }

    //println("ignored indices: " + ignoredIndices)
    //println("value from node: " + bestValue)
    (bestValue, bestTakeAll.toArray)
  }

  // sort with v/w desc, take from head until the exact capacity is reached, fill with fraction of last
  def estimate(values: List[Int], weights: List[Int], capacity: Int): Int = {
    //println("values before estimate: " + values)
    //println("weights before estimate: " + weights)
    if (values.isEmpty) 0
    else if (capacity <= 0) 0
    else {
      def vws = for (i <- Range(0, values.size)) yield (1.0 * values(i) / weights(i), values(i), weights(i))
      def sortedVws = vws.sortBy(vw => -vw._1 -> vw)

      //println(sortedVws)
      var idx = 0
      var sw = 0.0
      var sv = 0.0
      while (idx < sortedVws.length && sw <= capacity) {
        sw += sortedVws(idx)._3
        sv += sortedVws(idx)._2
        idx += 1
      }
      idx = idx - 1
      sw = sw - sortedVws(idx)._3
      sv = sv - sortedVws(idx)._2
      //println("index " + idx + " total weight: " + sw + " total value: " + sv)
      sv = sv + (capacity - sw) / sortedVws(idx)._3 * sortedVws(idx)._2
      //println("estimate: " + sv)
      sv.toInt
    }
  }
  // sum of values without those that can never fit into the sack
  def naiveEstimate(values: List[Int], weights: List[Int], capacity: Int): Int = {
    //println("values before estimate: " + values)
    //println("weights before estimate: " + weights)

    values.foldLeft(0)(_ + _)
  }

}

class Node(val value: Int, val room: Int, val estimate: Int, val idx: Int, val takes: List[Int]) extends Ordered[Node] {
  // decending order by estimate, then value
  def compare(that: Node) = {
    if (that.estimate == this.estimate) {
      if (that.value == this.value) 0
      else if (that.value > this.value) 1
      else -1
    } else if (that.estimate > this.estimate) 1
    else -1
  }
  override def toString(): String = "[" + value + ", " + room + ", " + estimate + ", " + idx + ", " + takes + "]"
}
