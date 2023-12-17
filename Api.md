# Api summary

This section shortly summarizes proposed api of strict collections.


## StrictSet

1. Read and other set-like operations
- contains, intersect, union, subsetOf, ...

2. Strict mutation (throwing on duplicate/non-existence)
- add/+, addAll/concat/++, removed/-, removedAll/--

3. Set-like mutation
- incl, union, excl, diff

4. Builder
- defaults to strict add/addAll
- this results in all iterable-based transformations being also strict
- provides also incl/inclAll for dropping duplicates


## StrictMap

1. Read and other map-like operations
- apply, get, keySet, ...

2. Strict mutation (throwing on duplicate/non-existence)
- add, removed

3. Map-like mutation
- updated/put, putAll, incl, excl

4. Builder
- addOne/addAll (strict, default)
- putOne/putAll (map-like update)


