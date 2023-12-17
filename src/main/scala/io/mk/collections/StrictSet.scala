package io.mk.collections

import scala.annotation.nowarn
import scala.collection.{IterableFactory, IterableOps, immutable, mutable}

/** Collection storing unique elements, disallowing attempts to add duplicates unless explicitly requested.
 *
 * Similar to Set, but with different growth/shrink semantics.
 * There are two groups of operations available:
 * - Strict (add/+, addAll/concat/++, remove/-, removeAll/--)
 * - Set-like (incl, union, excl, diff) - ignore duplicates and non-existence
 *
 * Note builder, and all transformations are strict according to
 * distinction above. This means:
 * `StrictSet.from(Seq("a","a"))` fails
 * `StrictSet("a","A").map(_.toUpperCase)` fails
 *
 * Such approach allows to be like well-behaving collection:
 * - c.map(...) preserving size
 * - no glitches on A having non-strict equality (in that case its unspecified which instance will actually be stored)
 *
 * I believe this is `that's the way it's always been` problem, that asks for some review.
 * `https://github.com/scala/bug/issues/11580`
 * `https://github.com/lampepfl/dotty/issues/6711`
 *
 * `StrictSet` comes in pair with `StrictMap` with similar behavior.
 *
 * Note `StrictSet` cannot implement `Set` interface due to original assumptions
 * specified and coded in.
 */
final class StrictSet[A] private(elems: Set[A])
  extends Iterable[A]
    with StrictSetOps[A, StrictSet, StrictSet[A]]
    with collection.IterableFactoryDefaults[A, StrictSet] {

  override def equals(that: Any): Boolean =
    (this eq that.asInstanceOf[AnyRef]) || (that match {
      case set: Set[A @unchecked] if set.canEqual(this.elems) => set.equals(elems)
      case set: StrictSet[A @unchecked] if set.toSet.canEqual(this.elems) => set.toSet.equals(elems)
      case _ => false
    })

  override def hashCode(): Int = elems.hashCode()

  override def toSet[B >: A]: scala.collection.immutable.Set[B] = elems.toSet[B]

  override def contains(elem: A): Boolean = elems.contains(elem)

  override def iterableFactory: IterableFactory[StrictSet] = StrictSet

  override def incl(elem: A): StrictSet[A] = new StrictSet(elems.incl(elem))

  override def excl(elem: A): StrictSet[A] = new StrictSet(elems.excl(elem))

  override def iterator: Iterator[A] = elems.iterator

  override def diff(that: StrictSet[A]): StrictSet[A] = new StrictSet(elems.removedAll(that))

  /** Removes element from StrictSet, failing if element is not there.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def removed(elem: A): StrictSet[A] = {
    if (!elems.contains(elem)) throw new IllegalArgumentException(s"Element $elem not found.")
    new StrictSet(elems.excl(elem))
  }

  /** Removes all elements from supplied collection, failing if any of them is not found in input set.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def removedAll(that: IterableOnce[A]): StrictSet[A] = {
    new StrictSet(that.iterator.foldLeft(elems) { (elems, elem) =>
      if (!elems.contains(elem)) throw new IllegalArgumentException(s"Element $elem not found.")
      elems.excl(elem)
    })
  }

  /** Creates a new $coll by adding element, failing if element is already contained.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def add(elem: A): StrictSet[A] = {
    if (elems.contains(elem)) throw new IllegalArgumentException(s"Element $elem already exists.")
    new StrictSet(elems.incl(elem))
  }

  /** Creates a new $coll by adding all elements contained in another collection to this $coll, failing if duplicates are found.
   *
   * This method takes a collection of elements and adds all elements, failing if duplicate is found.
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  override def addAll(that: IterableOnce[A]): StrictSet[A] = {
    new StrictSet(that.iterator.foldLeft(elems) { (elems, elem) =>
      if (elems.contains(elem)) throw new IllegalArgumentException(s"Element $elem already exists.")
      elems.incl(elem)
    })
  }

  override def inclAll(that: IterableOnce[A]): StrictSet[A] = new StrictSet(elems.concat(that))

  override def exclAll(that: IterableOnce[A]): StrictSet[A] = new StrictSet(elems.removedAll(that))

  @nowarn("""cat=deprecation&origin=scala\.collection\.Iterable\.stringPrefix""")
  override protected[this] def stringPrefix: String = "StrictSet"

  override def toString(): String = super[Iterable].toString() // Because `Function1` overrides `toString` too
}


/** Base trait for StrictSet operations.
 *
 * Close copy of `SetOps` (see comments on differences).
 *
 * @define coll strictSet
 * @define Coll `StrictSet`
 */
trait StrictSetOps[A, +CC[_], +C <: StrictSetOps[A, CC, C]]
  extends IterableOps[A, CC, C]
    with Equals
    with (A => Boolean) {

  def canEqual(that: Any) = true

  def contains(elem: A): Boolean

  @`inline` final def apply(elem: A): Boolean = this.contains(elem)

  @`inline` final def subsetOf(that: StrictSet[A]): Boolean = this.forall(that)

  @`inline` final def intersect(that: StrictSet[A]): C = this.filter(that)

  @`inline` final def &(that: StrictSet[A]): C = intersect(that)

  /** Returns elements from `this` that are not present in `that`
   * TODO: diff(that: Set[A])
   */
  def diff(that: StrictSet[A]): C

  @`inline` final def &~(that: StrictSet[A]): C = this diff that

  /** Removes element from StrictSet, failing if element is not there.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  def removed(elem: A): C

  /** Removes all elements from supplied collection, failing if any of them is not found in input set.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  def removedAll(that: collection.IterableOnce[A]): C

  /** Alias for `removedAll`
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  @`inline` final def --(that: IterableOnce[A]): C = removedAll(that)

  /** Alias for `remove`
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  @`inline` final def -(elem: A): C = removed(elem)

  /** Creates a new $coll by adding element, failing if element is already contained.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  def add(elem: A): C

  /** Creates a new $coll by adding all elements contained in another collection to this $coll, failing if duplicates are found.
   *
   * This method takes a collection of elements and adds all elements, failing if duplicate is found.
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  def addAll(that: collection.IterableOnce[A]): C

  /** Alias for `addAll`.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  @`inline` final def concat(that: collection.IterableOnce[A]): C = addAll(that)

  /** Alias for `add`.
   *
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  @`inline` final def +(elem: A): C = add(elem)

  /** Alias for `addAll` / `concat`.
   * This is one of `strict` operations that makes StrictSet behave differently than `Set`.
   */
  @`inline` final def ++(that: collection.IterableOnce[A]): C = addAll(that)

  /** Creates a new set with an additional element, unless the element is
   * already present.
   */
  def incl(elem: A): C

  def inclAll(that: collection.IterableOnce[A]): C

  /** Creates a new set with a given element excluded from this set. */
  def excl(elem: A): C

  /** Creates a new set with elements excluded from this set. */
  def exclAll(that: collection.IterableOnce[A]): C

  /** Computes the union between this and another set.
   * TODO: union(that: Set[A])
   */
  def union(that: StrictSet[A]): C = inclAll(that)

  /** Alias for `union` */
  @`inline` final def |(that: StrictSet[A]): C = union(that)
}


object StrictSet extends collection.IterableFactory[StrictSet] {
  override def empty[A]: StrictSet[A] = new StrictSet(Set.empty[A])

  override def from[A](source: IterableOnce[A]): StrictSet[A] = source match {
    case s: StrictSet[A] => s
    case s: immutable.Set[A] => new StrictSet(s)
    case s => StrictSet.newBuilder[A].addAll(s).result()
  }

  /** StrictSet builder, specific as it adds methods for non-strict building. */
  override def newBuilder[A]: StrictSetBuilder[A] = new StrictSetBuilder[A]
}

/** Builder for StrictSets.
 *
 * Provides two scents:
 * - addOne/addAll strict, fail when trying to add element twice (part of mutable.Builder interface)
 * - incl/inclAll as in `Set`, silently drop duplicates (set-like non-strict builder)
 *
 * Actually one might consider if those methods are needed if original `Set` is around.
 * For non-strict building, one can just build `Set`, and then convert it to StrictSet if needed.
 */
final class StrictSetBuilder[A] extends mutable.Builder[A, StrictSet[A]] {
  private[this] var elems: Set[A] = Set.empty

  override def clear(): Unit = elems = Set.empty

  override def result(): StrictSet[A] = StrictSet.from(elems)

  def addOne(elem: A): StrictSetBuilder.this.type = {
    if (elems.contains(elem)) throw new IllegalArgumentException(s"Element $elem already exists.")
    elems = elems.incl(elem)
    this
  }

  override def addAll(xs: IterableOnce[A]): this.type = {
    xs.iterator.foreach(addOne)
    this
  }

  def incl(elem: A): StrictSetBuilder.this.type = {
    elems = elems.incl(elem)
    this
  }

  def inclAll(xs: IterableOnce[A]): this.type = {
    elems = elems.concat(xs)
    this
  }
}
