## sbasalaev-common

[![Latest release](https://img.shields.io/github/v/release/sbasalaev/sbasalaev-common)](https://github.com/SBasalaev/sbasalaev-common/releases/latest)
[![Javadoc](https://img.shields.io/badge/javadoc-orange)](https://api.sbasalaev.me/sbasalaev-common)

Common goodies that I use across my projects.  These are everyday features that
are not present in Java or were not available at the time of writing or just
some alternate APIs I comfortable with. The library includes:

* Custom collections with a clear separation between read and write APIs, such as
  [List](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/collection/List.html)
  which only has read methods
  and [MutableList](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/collection/MutableList.html)
  which also has modification methods.
* Functional transformations on collections through
  [Traversable](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/collection/Traversable.html).
  ```java
  list("A", "BB", "CCC").map(String::length).fold(0, Integer::sum)
  ```
  Java 1.8 introduced Stream API which is much more powerful.
* Multimaps: [ListMultimap](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/collection/ListMultimap.html)
  and [SetMultimap](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/collection/SetMultimap.html).
  [Map](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/collection/Map.html)
  is also a kind of multimap and shares common API with them.
* [Opt](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/Opt.html)
  for optional values. Java 1.8 introduced Optional that serves the same purpose
  but is not a collection. I sometimes find it convenient to process an optional
  value like
  ```java
  for (var value : optionalValue) {
      // do something
  }
  ```
* [Lazy](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/Opt.html)
  for lazily evaluated values.
* [TODO()](https://api.sbasalaev.me/sbasalaev-common/me.sbasalaev.common/me/sbasalaev/API.html#TODO(java.lang.String))
  – one of the most useful functions during development.
