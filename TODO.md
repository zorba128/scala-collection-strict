# Evolution ideas, TODOs

## Implementation

By no means this is ready-made implementation. Whole project is just POC
to start some discussion. For me, it is quite complex task to fit in Map-like
collection into scala collection library without use of ready-made building blocks.
And those cannot be used due to requirements conflicts.

Many elements are missing - like strict mutable collections.
Number of changes needed, compared to original `Set` and `Map` is not
that big - as mutable collections somehow already support those scenarios.
They just miss some convenience methods that would automatically enforce
constraints, rather than leaving that to user. 

As an example: `mutable.Map.put(k,v): Option[V]` and `mutable.Set.add(e)`
both signal if element was there or not. For immutable collections this
is impossible, as methods applying updates have to return
updated collection itself. So only way to handle that is to have as specific
methods as possible, potentially throwing if precondition is not met.

Also for now `StrictMap` and `StrictSet` are ready-made implementations,
while this should become another type of extensible collection interface.

## Naming

This will be one most complex issues to solve. `Strict` already
has meaning in scala. Method naming is also kind of first proposal.

## Common interface between strict and non-strict collections

Problem is that Set/Map interface specifies both read and write behavior.
And while read access for Set and StrictSet is identical, different
mutation policies make it impossible to use them interchangeably.

Maybe it might be useful to have something like `trait Distinct[T] extends Iterable[T]`
that would mark inputs that have uniqueness guarantee.
Note it fits to many places, like Seq.distinct. 



