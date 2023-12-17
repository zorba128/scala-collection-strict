package io.mk.collections

import org.scalatest.funsuite.AnyFunSuite

class StrictSetTests extends AnyFunSuite {
  test("Basic operations") {
    val a = StrictSet("a", "b")
    assert(a.contains("a"))
    assert(!a.contains("x"))
    assert(a.size == 2)

    intercept[IllegalArgumentException](StrictSet("a", "a"))
  }

  test("Grow strictness") {
    val a = StrictSet("a", "b")
    intercept[IllegalArgumentException](a.add("a"))
    assert(a.incl("a").size == 2)

    assert(a.union(StrictSet("a", "c")).size == 3)
    intercept[IllegalArgumentException](a.concat(StrictSet("a", "c")))
  }

  test("Builder strictness") {
    val builder = StrictSet.newBuilder[String]
    builder.addOne("a")
    intercept[IllegalArgumentException](builder.addOne("a"))
    builder.incl("a")
    assert(builder.result() === StrictSet("a"))
  }


  test("Removal strictness") {
    val a = StrictSet("a", "b")
    intercept[IllegalArgumentException](a.removed("X"))
    assert(a.excl("X").size == 2)

    assert(a.diff(StrictSet("a", "c")).size == 1)
    intercept[IllegalArgumentException](a.removedAll(StrictSet("a", "c")))
  }


  test("Transformation strictness") {
    val a = StrictSet("a", "A")
    val b = a.map(_.repeat(2))
    assert(b === StrictSet("aa", "AA"))
    intercept[IllegalArgumentException] {a.map(_.toUpperCase)}
  }
}