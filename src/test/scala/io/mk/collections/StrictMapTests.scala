package io.mk.collections

import org.scalatest.funsuite.AnyFunSuite

class StrictMapTests extends AnyFunSuite {
  test("Basic operations") {
    val a = StrictMap("a" -> 1, "b" -> 2)
    println(a)
    assert(a.contains("a"))
    assert(!a.contains("x"))
    assert(a.size == 2)

    intercept[IllegalArgumentException](StrictMap("a" -> 1, "a" -> 2))
    intercept[IllegalArgumentException](StrictMap("a" -> 1, "a" -> 1))
  }

  test("Grow strictness") {
    val a = StrictMap("a" -> 1, "b" -> 2)
    intercept[IllegalArgumentException](a.add("a", 3))
    assert(a.updated("a", 2).size == 2)
    intercept[IllegalArgumentException](a.concat(StrictMap("a" -> 2, "c" -> 3)))
  }

  test("Builder strictness") {
    val builder = StrictMap.newBuilder[String, Int]
    builder.addOne("a" -> 1)
    intercept[IllegalArgumentException](builder.addOne("a" -> 2))
    builder.putOne("a" -> 2)
    assert(builder.result() === StrictMap("a" -> 2))
  }


  test("Removal strictness") {
    val a = StrictMap("a" -> 1, "b" -> 2)
    intercept[IllegalArgumentException](a.removed("X"))
    assert(a.excl("X").size == 2)
  }

  test("Transformations strictness") {
    val A = Map("a" -> 1, "A" -> 2)
    val a = StrictMap("a" -> 1, "A" -> 2)
    println(a.filter(_._1 == "a"))
  }


  test("Transformation strictness") {
    val a = StrictMap("a" -> 1, "A" -> 2)
    val b = a.map { case (k, v) => k.repeat(2) -> v * 11 }
    assert(b === StrictMap("aa" -> 11, "AA" -> 22))
    intercept[IllegalArgumentException] {
      a.map { case (k, v) => k.toUpperCase -> v }
    }
  }
}