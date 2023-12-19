package io.mk.collections

import org.scalatest.funsuite.AnyFunSuite

/** Shows how proposed approach solves `https://github.com/scala/bug/issues/11580`.
 */
class ScalaIssue11580 extends AnyFunSuite {
  test("OriginalCollections") {
    val elements = Set(1,2,3)
    val weights = Map(1 -> 1.0, 2 -> 1.0, 3 -> 1.0)
    val values = Map(1 -> 0.5, 2 -> 0.5, 3 -> 0.5)

    val wSum = elements.map { element =>
        weights(element) * values(element)
      }.sum

    pendingUntilFixed {
      assert(wSum == 1.5) //fails, 0.5 did not equal 1.5
    }
  }

  test("StrictCollections") {
    val elements = StrictSet(1,2,3)
    val weights = StrictMap(1 -> 1.0, 2 -> 1.0, 3 -> 1.0)
    val values = StrictMap(1 -> 0.5, 2 -> 0.5, 3 -> 0.5)

    // Element 0.5 already exists.
    intercept[IllegalArgumentException] {
      val wSum = elements.map { element =>
        weights(element) * values(element)
      }.sum
      assert(wSum == 1.5)
    }
  }
}
