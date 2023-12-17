package io.mk.collections

import io.mk.collections.Index.EvidenceIndexFactory

import scala.collection.{EvidenceIterableFactory, EvidenceIterableFactoryDefaults, IterableFactory, IterableFactoryDefaults, mutable}

trait Indexer[K, V] extends (V => K)

object Indexer {
  def from[K, V](f: V => K): Indexer[K, V] = v => f(v)

  def apply[V] = new Builder[V]

  def identity[V]: Indexer[V, V] = v => v

  class Builder[V] {
    def apply[K](f: V => K): Indexer[K, V] = new Indexer[K, V] {
      def apply(v: V): K = f(v)
    }
  }
}

/** Index data structure, storing elements by unique index function.
 *
 * Its like `Set` that uses function to extract key from values.
 * Or its like `Map` that derives key from value.
 *
 * Uses `Map` as underlying data structure. Constructor contract
 * (unchecked, ensured by builder/constructors),
 * is that Map passed in uses mapping compatible with indexer.
 *
 * Note, given `k=f(v)` and knowing keys are unique, values are also unique.
 * So Index[K,V] is both Set[K] and Set[V].
 *
 * @see scala.collection.immutable.SortedSet for example of collection with evidence
 */
final class Index[K, V] private(elems: Map[K, V])(implicit val indexer: Indexer[K, V])
  extends Iterable[V]
     with StrictSetOps[V, Index[K, *], Index[K, V]]
     with collection.IterableFactoryDefaults[V, Index[K, *]]
     with EvidenceIterableFactoryDefaults[V, Index[K, *], Indexer[K, *]] {

  /** Returns plain scala set.
   * TODO: custom implementation as view on this.
   */
  override def toSet[B >: V]: Set[B] = elems.values.toSet

  /** Returns plain scala map. */
  def toMap: Map[K, V] = elems

  override def iterator: Iterator[V] = elems.valuesIterator.iterator

  override def iterableFactory: IterableFactory[Index[K, *]] = ???

  override protected def evidenceIterableFactory: EvidenceIterableFactory[Index[K, *], Indexer[K, *]] = new EvidenceIndexFactory[K]

  override protected implicit def iterableEvidence: Indexer[K, V] = indexer

  override val className = "Index"

  override def contains(elem: V): Boolean = elems.get(indexer(elem)).contains(elem)

  def containsKeyOf(elem: V): Boolean = elems.contains(indexer(elem))

  def containsKey(key: K): Boolean = elems.contains(key)

  override def incl(elem: V): Index[K, V] = new Index(elems.updated(indexer(elem), elem))(indexer)

  override def excl(elem: V): Index[K, V] = new Index(elems.removed(indexer(elem)))(indexer)

  /** Returns elements from `this` that are not present in `that`
   * TODO: diff(that: Set[A])
   */
  override def diff(that: StrictSet[V]): Index[K, V] = new Index(elems.removedAll(that.map(indexer(_))))(indexer)

  /** Removes element from StrictSet, failing if element is not there.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def removed(elem: V): Index[K, V] = ???

  /** Removes all elements from supplied collection, failing if any of them is not found in input set.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def removedAll(that: IterableOnce[V]): Index[K, V] = ???

  /** Creates a new $coll by adding element, failing if element is already contained.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def add(elem: V): Index[K, V] = ???

  /** Creates a new $coll by adding all elements contained in another collection to this $coll, failing if duplicates are found.
   *
   * This method takes a collection of elements and adds all elements, failing if duplicate is found.
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def addAll(that: IterableOnce[V]): Index[K, V] = ???

  override def inclAll(that: IterableOnce[V]): Index[K, V] = ???

  /** Creates a new set with elements excluded from this set. */
  override def exclAll(that: IterableOnce[V]): Index[K, V] = ???
}

object Index {
  def empty[K, V](implicit indexer: Indexer[K, V]): Index[K, V] = new Index(Map.empty)

  def apply[K, V](elems: V*)(implicit indexer: Indexer[K, V]): Index[K, V] = new Index(Map.from(elems.map(elem => indexer(elem) -> elem)))

  def from[K, V](it: IterableOnce[V])(implicit indexer: Indexer[K, V]) = new Index(Map.from(it.iterator.map(elem => indexer(elem) -> elem)))

  def newBuilder[K, V](implicit indexer: Indexer[K, V]): mutable.Builder[V, Index[K, V]] = new IndexBuilder[K, V]

  private final class IndexBuilder[K, V](implicit indexer: Indexer[K, V]) extends mutable.Builder[V, Index[K, V]] {
    private[this] val builder = Map.newBuilder[K, V]

    override def clear(): Unit = builder.clear()

    override def result(): Index[K, V] = new Index(builder.result())

    override def addOne(elem: V): this.type = {
      builder.addOne(indexer(elem), elem)
      this
    }

    override def addAll(xs: IterableOnce[V]): this.type = {
      builder.addAll(xs.iterator.map(v => indexer(v) -> v))
      this
    }
  }

  /** Factory requesting implicit Indexer. */
  private final class EvidenceIndexFactory[K] extends EvidenceIterableFactory[Index[K, *], Indexer[K, *]] {
    override def from[V: Indexer[K, *]](it: IterableOnce[V]): Index[K, V] = Index.from(it)

    override def empty[V: Indexer[K, *]]: Index[K, V] = Index.empty

    override def newBuilder[V: Indexer[K, *]]: mutable.Builder[V, Index[K, V]] = new IndexBuilder[K, V]
  }
}


object IndexTests extends App {
  case class Elem(text: String)

  implicit val indexer: Indexer[String, Elem] = Indexer[Elem](_.text.take(1))

  val a: Index[String, Elem] = Index(Elem("aaaa"), Elem("bbbb"))
  println(a)
  println(a.toMap)
  println(a.toSet)
  // println(a.toSet)
  println(a.filter(_.text.endsWith("b")))

  println(a.incl(Elem("ccccc")))
  println(a.incl(Elem("aAAAAA")))
  println(a.excl(Elem("aAAAAA"))) // this should fail!
}
