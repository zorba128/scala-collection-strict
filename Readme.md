# scala-collection-strict

This module provides custom alternative to `Map`/`Set` collections from scala library.
It is still far from being production-ready, its main goal is to propose
and evaluate alternative approach.

## Explanation

Proposal is to make `Set` and `Map` builders throw exception on duplicates
rather dropping them silently.
We all are used to current behavior (it's been there since java 1.0)
but maybe its high time to at least discuss if this was the best choice.

Of course old behavior has to stay available - it’s still needed to be able to easily include 
element in the set, regardless if it was already there or not. 
But I’d like to propose to separate those apis. Let `Set.add()` add elements, 
and `Set.incl()` ensure element is included.

[Story](Story-on-strictness.md) document provides more detailed discussion of the idea.

## Usage

Collections are in the `io.mk.collections` package. 
For now only immutable variants are provided.

- `StrictSet`
- `StrictMap`

Basic example illustrating the idea
```scala
  val a = StrictSet("a", "A")
  a.incl("a") // StrictSet("a","A")
  a.add("a") // duplicate element exception
  a union StrictSet("a", "b") // StrictSet("a","A","b")
  a concat StrictSet("a", "b") // duplicate element exception
  
  a.map(_.toUpperCase) // duplicate element exception
  StrictSet("a", "a") // duplicate element exception
```

See [Api](Api.md) for short summary of what is in there.

Take a look at [TODO](TODO.md) for list of improvements pending.
