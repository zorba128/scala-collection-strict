package io.mk.collections

import org.scalatest.funsuite.AnyFunSuite

class VarianceTests extends AnyFunSuite {
  trait X
  case class Y(y: Int) extends X
  case class Z(z: Boolean) extends X

  test("StrictMap growth") {
    val cm1: Map[String, X] = Map("a" -> Y(1), "b" -> Z(true) )
    val sm1: StrictMap[String, X] = StrictMap("a" -> Y(1), "b" -> Z(true) )

    val cm2: Map[String, X] = Map("a" -> Y(1)).updated("b", Z(true))
    val sm2: StrictMap[String, X] = StrictMap("a" -> Y(1)).updated("b", Z(true))
  }

  test("StrictSet growth") {
    val cm1: Map[String, X] = Map("a" -> Y(1), "b" -> Z(true) )
    val sm1: StrictMap[String, X] = StrictMap("a" -> Y(1), "b" -> Z(true) )

    val cm2: Map[String, X] = Map("a" -> Y(1)).updated("b", Z(true))
    val sm2: StrictMap[String, X] = StrictMap("a" -> Y(1)).updated("b", Z(true))
  }

}
