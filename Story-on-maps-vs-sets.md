# Set and Map similarities and differences

`Set` and `Map` are closely related data structures.
Map has `keySet`, `Map[V,V]` is sometimes used as backing store for `Set`, many more similarities probably
could be found. But also there are some differences
- why `Set.add(e)` and `Map.add(e, v)` for existing entry - for set is no-op and for map is update?
- this leads to unexplainable (other than by specific implementation) behavior of `Set.from(it)` and `Map.from(it)`
  producing different results (set contains first occurrence, while map last one).

This all somehow works, but does not fit as nicely as it could.

### Lookups

Anyone wrote/seen code like this:
```val lookup = data.map(e => e.id -> e).toMap```

Always wondered why there is no ready-made
```Map.index[K,V](it: Iterable[V])(index: V => K)```

### Features

```
Index[A](identity) === Set[A]
Index[(K,V)](_._1) === Map[K,V]
```

### Problems

How to handle removal?
- look up key
- check if element actually removed is the one being removed