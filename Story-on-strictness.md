# How it all started

I believe most of us feel something is wrong with code below
```scala
val a = Set("a", "a")
println(a.size)
```

Everyone is used to sets disallowing duplicate elements (and dropping them silently),
but it still does not feel right. If user intention was to create set of two elements,
so why size of result is `1`?

Similar case with Map
```scala
val a = Map("red" -> Color(1,0,0), "green" -> Color(0,1,0), "red" -> Color(0.9,0,0))
```
We all know map will pick the faint color for "red" key. But do you really believe this 
was the intention of person who wrote this code? Why did he provide two `red` entries
knowing this will be dropped? Maybe he simply made a mistake - but in that case
one would expect exception to signal something is wrong.

I was amazed how many times I actually solved same problem - building
immutable map/set from data and failing on duplicates. This is not straightforward
with scala collections - one cannot use ready-made `Builder` for it. 
I usually end up implementing builder-like utility that under the hood uses mutable
map/set in order to do enforce checks.

I was amazed even more when I realized how many times I should be doing above,
and I simply ignored the problem, allowing duplicates to be dropped silently, justifying by
- here it should not happen
- its user error if this happens
- lets just document client should ensure no duplicates, otherwise result is undefined

Like here
```scala
  val a = new java.util.Properties()
  a.load(new StringReader("a=1\nb=2\na=3"))
  println(a)
```

Why it is allowed to specify same property two times in single file?
This should be reported as exception, rather than silently swallowing
first assignment. Really - I see no reason to do it, other than
`its always been like that`. We're all bound by classic algebraic
`set` definition, what makes simple real-life problems hard to solve...


This behavior leads to several counter-intuitive properties of Sets/Maps.
Like `map` operation silently dropping collection elements:
```Set("a","A").map(_.toUpperCase)```
```Map("a" -> 1,"A" -> 2).map { case (k,v) => k.toUpperCase -> v }```

There were several discussions over time on how to specify this behavior in documentation,
and how to warn users this can happen. And again - while everyone is already used to it,
I think the root problem is the core assumption behind that behavior.
High level requirement is `set/map cannot store same element/key twice`.
And practical implementation that was derived from it:
 - `adding element to set that already contains that element is no-op`
 - `adding entry with key that already exists in map overwrites it`

Actually for 1st one I'm not so sure - I need to check it :). This also shows how fragile
that approach is.

# Idea

I think rather than n-th time trying to overcome or explain this behavior, let's just fix it at the root.
Note `set/map cannot store same element/key twice` can be rendered into
- `adding element to set that already contains that element is impossible and results in exception`
- `adding entry with key that already exists in map is impossible and results in exception`

Of course, we still need possibility to safely include elements in sets regardless of their existence,
to union sets dropping duplicates, to update map entries for new value. Core point is
`Set.add(e)` is different operation than `Set.incl(e)`. And default behavior should be the first 
one, because its specification is strict, keeping other one for situations where user 
explicitly calls for it.

Project provides proposal of `StrictSet` and `StrictMap` collections that follow that idea.
Note `StrictSet` cannot implement `Set` interface, and `StrictMap` cannot implement `Map`
because original assumptions on behavior are violated. Api that was proposed was designed to 
maintain original semantics as much as possible, just changing what was really necessary.

# Builder contract

It's all about meaning of `adding element to the set` operation.
Most languages and libraries (please point me at some language that does it different way)
assume this is algebraic set union. But for me this is wrong;
if I write `a.add(x)` I want X to become part of `a`. If this is impossible for some reason
(set already contains `a`) - I'd expect failure to be reported.

Note we already have tools to do it in convenient way:
```scala
Set.from(Seq(1,2,3,1,1).distinct)
```

I believe this can be implemented so that no unnecessary distinction check happens.

Maybe the name of collection might be `DistinctCollection` - `Collection not allowing duplicates to be added`.

# Final words

I know will be hard task to fit this idea into existing collection system. 
But maybe it is time to revise design that was derived from 40 years old books.

