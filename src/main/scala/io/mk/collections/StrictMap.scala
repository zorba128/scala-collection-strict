package io.mk.collections

import scala.annotation.nowarn
import scala.collection.{IterableOps, MapFactory, immutable, mutable}


/** Map storing unique elements, disallowing attempts to add duplicates unless explicitly requested.
 *
 * Similar to Map, but with different growth/shrink semantics.
 * There are two groups of operations available:
 * - Strict (add/+, addAll/concat/++, remove/-, removeAll/--)
 * - Map-like (incl, union, excl, diff) - ignore duplicates and non-existence
 *
 * Note builder, and all transformations are strict according to
 * distinction above. This means:
 * `StrictMap("a" -> 1, "a" -> 2)` fails
 * `StrictMap("a" -> 1, "A" -> 2).mapKeys(_.toUpperCase)` fails
 *
 * I believe this is `that's the way it's always been` problem, that asks for some review.
 * `https://github.com/scala/bug/issues/11580`
 * `https://github.com/lampepfl/dotty/issues/6711`
 *
 * `StrictMap` comes in pair with `StrictSet` with similar behavior.
 *
 * Note `StrictMap` cannot implement `Map` interface due to original assumptions
 * specified and coded in.
 */
final class StrictMap[K, V] private(elems: Map[K, V])
  extends Iterable[(K, V)]
    with StrictMapOps[K, V, StrictMap, StrictMap[K, V]] {

  override def equals(that: Any): Boolean =
    (this eq that.asInstanceOf[AnyRef]) || (that match {
      case map: Map[K @unchecked, V @unchecked] if map.canEqual(this.elems) => map.equals(elems)
      case map: StrictMap[K @unchecked, V @unchecked] if map.toMap.canEqual(this.elems) => map.toMap.equals(elems)
      case _ => false
    })

  override def hashCode(): Int = elems.hashCode()

  override def toMap[K2, V2](implicit ev: (K, V) <:< (K2, V2)): Map[K2, V2] = elems.toMap[K2, V2]


  override def iterator: Iterator[(K, V)] = elems.iterator

  @nowarn("""cat=deprecation&origin=scala\.collection\.Iterable\.stringPrefix""")
  override protected[this] def stringPrefix: String = "StrictMap"

  override def toString(): String = super[Iterable].toString() // Because `Function1` overrides `toString` too

  override def contains(key: K): Boolean = elems.contains(key)

  override def apply(key: K): V = elems(key)

  override def get(key: K): Option[V] = elems.get(key)

  def add(elem: (K, V)): StrictMap[K, V] = add(elem._1, elem._2)

  override def add[V2 >: V](key: K, value: V2): StrictMap[K, V2] = {
    if (elems.contains(key)) throw new IllegalArgumentException(s"Key $key already exists")
    new StrictMap(elems.updated(key, value))
  }

  /** Add if nonexisting, throw otherwise. */
  override def concat[V2 >: V](suffix: IterableOnce[(K, V2)]): StrictMap[K, V2] = {
    new StrictMap(suffix.iterator.foldLeft(elems.asInstanceOf[Map[K, V2]]) { (elems, elem) =>
      if (elems.contains(elem._1)) throw new IllegalArgumentException(s"Key ${elem._1} already exists")
      elems.updated(elem._1, elem._2)
    })
  }

  /** Add or update. */
  override def updated[V2 >: V](key: K, value: V2): StrictMap[K, V2] = new StrictMap(elems.updated(key, value))

  /** Removes element, fails if non existing. */
  override def removed(key: K): StrictMap[K, V] = {
    if (!elems.contains(key)) throw new IllegalArgumentException(s"Key $key not found")
    new StrictMap(elems.removed(key))
  }

  /** Remove if existing. Like filterNot. */
  override def excl(key: K): StrictMap[K, V] = new StrictMap(elems.removed(key))

  /** Builds a new map by applying a function to all elements of this $coll.
   *
   * @param f the function to apply to each element.
   * @return a new $coll resulting from applying the given function
   *         `f` to each element of this $coll and collecting the results.
   */
  override def map[K2, V2](f: ((K, V)) => (K2, V2)): StrictMap[K2, V2] = StrictMap.from[K2, V2](elems.iterator.map(f))

  override def empty: StrictMap[K, V] = StrictMap.empty

  override def newSpecificBuilder: StrictMapBuilder[K, V] = StrictMap.newBuilder[K, V]

  override protected def fromSpecific(coll: IterableOnce[(K, V)]): StrictMap[K, V] = StrictMap.from(coll)

  override def keySet: StrictSet[K] = StrictSet.from(elems.keySet)

  override def valueSet: StrictSet[V] = keySet.map(elems)

  override def valuesIterator: Iterator[V] = elems.valuesIterator
}

trait StrictMapOps[K, V, +CC[X, Y] <: StrictMapOps[X, Y, CC, _], +C <: StrictMapOps[K, V, CC, C]]
  extends IterableOps[(K, V), Iterable, C] {
  protected def coll: C with CC[K, V]

  def contains(key: K): Boolean

  def apply(key: K): V

  def get(key: K): Option[V]

  /** Add if nonexisting, throw otherwise. */
  def add[V2 >: V](key: K, value: V2): CC[K, V2]

  /** Add if nonexisting, throw otherwise. */
  def concat[V2 >: V](suffix: collection.IterableOnce[(K, V2)]): CC[K, V2]

  /** Add or update. */
  def updated[V2 >: V](key: K, value: V2): CC[K, V2]

  /** Remove if existing, throw otherwise. */
  def removed(key: K): C

  /** Remove if existing. */
  def excl(key: K): C

  /** Builds a new map by applying a function to all elements of this $coll.
   *
   * @param f the function to apply to each element.
   * @return a new $coll resulting from applying the given function
   *         `f` to each element of this $coll and collecting the results.
   */
  def map[K2, V2](f: ((K, V)) => (K2, V2)): CC[K2, V2]

  def keySet: StrictSet[K]

  def valueSet: StrictSet[V]

  def valuesIterator: Iterator[V]
}

object StrictMap extends MapFactory[StrictMap] {
  override def empty[K, V]: StrictMap[K, V] = new StrictMap(Map.empty[K, V])

  override def from[K, V](it: IterableOnce[(K, V)]): StrictMap[K, V] = it match {
    case s: StrictMap[K, V] => s
    case s: immutable.Map[K, V] => new StrictMap(s)
    case s => newBuilder[K, V].addAll(s).result()
  }

  override def newBuilder[K, V]: StrictMapBuilder[K, V] = new StrictMapBuilder[K, V]
}

class StrictMapBuilder[K, V] extends mutable.Builder[(K, V), StrictMap[K, V]] {
  private var elems = Map.empty[K, V]

  override def clear(): Unit = elems = Map.empty

  override def result(): StrictMap[K, V] = StrictMap.from(elems)

  def addOne(key: K, value: V): this.type = {
    if (elems.contains(key)) throw new IllegalArgumentException(s"Key $key already exists")
    elems = elems.updated(key, value)
    this
  }

  override def addOne(elem: (K, V)): StrictMapBuilder.this.type = {
    if (elems.contains(elem._1)) throw new IllegalArgumentException(s"Key ${elem._1} already exists")
    elems = elems.updated(elem._1, elem._2)
    this
  }

  override def addAll(xs: IterableOnce[(K, V)]): StrictMapBuilder.this.type = super.addAll(xs)

  def putOne(key: K, value: V): StrictMapBuilder.this.type = {
    elems = elems.updated(key, value)
    this
  }

  def putOne(elem: (K, V)): StrictMapBuilder.this.type = {
    elems = elems.updated(elem._1, elem._2)
    this
  }

  def putAll(it: IterableOnce[(K, V)]): StrictMapBuilder.this.type = {
    elems = elems.concat(it)
    this
  }
}