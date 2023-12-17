package io.mk.collections

import java.io.StringReader
import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Properties

object StrictExplanation extends App {
  // we cannot extent set
  val a = Set(1, 1)
  println(a)

  class El(val k: Int, val v: String) {
    override def equals(obj: Any): Boolean = obj match {
      case e: El => k.equals(e.k)
      case _ => false
    }

    override def hashCode(): Int = k

    override def toString: String = s"El($k,$v)"
  }

  object El {
    def apply(k: Int, v: String) = new El(k, v)
  }

  val in = Seq(El(1, "a"), El(1, "b"))

  val aaa = Set.from(in)
  val bbb = Map.from(in.map(e => e.k -> e))
  println(aaa)
  println(bbb)

  val b = Map("a" -> 1, "a" -> 2).removed("a")
  println(b)

  val bb = TreeMap("a" -> 1).removed("a")

  val c = StrictMap("a" -> 1, "b" -> 2)
  println(c.take(1))
}

object XXX extends App {
  val a = mutable.Map.empty[String, Int].put("a", 1)
}

object HyperCubeServer extends App {
  type C = Map[String, Int] // coordinates along named dimensions
  val cube = load(Paths.get("/data/cube.txt"))

  def load(source: Path): Map[C, Double] = ???

  def parseQuery(queryString: String): C = ???

  def getPoints(coordinates: C): Map[C, Double] = cube.view.filterKeys(key => coordinates.toSet subsetOf key.toSet).toMap
}

object HyperCubeServer2 extends App {
  type C = Map[String, Int] // coordinates along named dimensions
  val cube = load(Paths.get("/data/cube.txt")) // a:1;b:2=123.0

  def load(source: Path): Map[C, Double] = {
    Files.readAllLines(source).asScala
      .map(point => point.split(','))
    Map.empty
  }

  def parseCoordinates(coordinates: String): C = {
    if (coordinates.isEmpty)
      Map.empty
    else
      coordinates
        .split(';')
        .map { case s"$dim=$c" => dim -> c.toInt }
        .toMap
  }

  def getPoints(coordinates: C): Map[C, Double] = cube.view.filterKeys(key => coordinates.toSet subsetOf key.toSet).toMap
}

object JavaProperties extends App {
  val a = new java.util.Properties()
  a.load(new StringReader("a=1\nb=2\na=3"))
  println(a)
}

object XXX2 extends App {
  println(Map("a" -> 1,"A" -> 2).map { case (k,v) => k.toUpperCase -> v })
}