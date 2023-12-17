# scala-collection-strict

This module provides custom alternative to `Map`/`Set` collections from scala library.
It is still far from being production-ready, its main goal is to propose
and evaluate alternative approach.

## Explanation

[Story](Story-on-strictness.md) document tries to explain the reasoning behind whole idea.

## Usage

Collections are in the `io.mk.collections` package. 
For now only immutable variants are provided.

- `StrictSet`
- `StrictMap`
- `StrictMapSet`

See [Api](Api.md) for short summary of what is in there.

Take a look at [TODO](TODO.md) for list of improvements pending.
