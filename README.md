# pull

[![Build Status](https://travis-ci.org/robertluo/pull.svg?branch=master)](https://travis-ci.org/robertluo/pull)
[![Clojars Project](https://img.shields.io/clojars/v/robertluo/pull.svg)](https://clojars.org/robertluo/pull)
[![cljdoc badge](https://cljdoc.xyz/badge/robertluo/pull)](https://cljdoc.xyz/d/robertluo/pull/CURRENT)

> [Notice] Forked from JUXT pull. The maintainer does not respond with modification from me yet, however, 0.2.0 (which also submitted by me) has some inconsistent bugs and should be replaced by this library.

Clojure has powerful syntax for expressing nested data structures as trees.

Trees are extremely useful for user-interfaces, configuration and data transfer (EDN, JSON and XML).

However trees force a single perspective on the data. The 'root' is privileged. It is impossible to design tree models to support multiple perspectives.

That's why we prefer to store data in table rows, with links (foreign keys) between them. A database is agnostic about the perspective from which the data is to be viewed.

To define a specific perspective on data we define a query. Now we are back to a single perspective, a tree is an ideal container for that data.

This is an insight that a number of people have had. Datomic's pull API is an early example. David Nolen has taken inspiration from this to create Om Next's query expressions. However, the idea is too valuable and applicable to be enmeshed in narrower applications.

## Usage

Declare some state:

```clojure
'{:name "pull"
  :routes {:main ["/" :abc]}
  :vhosts {"http://localhost:8080" ^:ref [:routes :main]}
  :server {:port 8080
           :vhosts [^:ref [:vhosts "http://localhost:8080"]]}}
```

Write a query:

```clojure
[:name :vhosts]
```

Get a result:

```clojure
{:name "pull" :vhosts {"http://localhost:8080" ["/" :abc]}}
```

### Spec

`juxt.pull.spec` contains specs for the API. you can require it if you want to check.

## Queries

Queries a vectors, containing the entries you wish to pull out of the state.

## References

Note that the state contains references, indicated with the metadata tag `^:ref`. These are similar to Datomic lookup-refs or Om Next's idents. These references are vectors corresponding to the path you might use in a `get-in` function to access other parts of the state.

> From version 0.2.6, this behavior can be disabled by pull option `:no-ref? true`, which will speed things up if you do not need them.

## Joins

Joins (sub-queries) are also supported by providing a map in place of a keyword in a query.

## Nested maps of sequence

If a value is a sequence (sequential?) of map, query inside will return a vector of values, just like in Datomic's pull api.

```clojure
(pull {:person/name "Joe" :person/children [{:person/name "Bob"} {:person/name "Alice"}]}
      [:person/name {:person/children [:person/name]}])

;;=> {:person/name "Joe" :person/children [{:person/name "Bod"} {:person/name "Alice"}]}
```

## Shadow attributes (values)

You can define attributes (values) not exists but calculated by the value of the map, they are shadow attributes:

```clojure
(pull {:person/name "Joe" :person/children [{:person/name "Bob"} {:person/name "Alice"}]}
      [:person/name :person/num-kids]
      {:shadow {:person/num-kids #(-> % :person/children count)}})

;;=> {:person/name "Joe" :person/num-kids 2}
```

## Stealth (invisible) attributes

By defining stealth sets of keys, you can make some of the keys invisible.

```clojure
(pull {:user/name "foo" :user/password "secret"} [:user/name :user/password]
      {:stealth #{:user/password}})

;;=> {:user/name "foo"}
```

## Wildcard

Just like Datomic's pull API, you can use `'*` to get all attributes of a map. You can also turns it off by specific `:no-wildcard? true` in the options map.

## vs Datomic's pull API

The most important difference of course is the library can be used on any map or maps, where Datomic's pull can only query `EntityMaps`.

You can use either this library or Datomic's builtin Pull API if you want to pull Datomic entities. Considering following:

 - Datomic's Pull has `:limit` feature for paging.
 - Datomic's Pull has `:as` to rename attribute, `:default` for default value, which can achieve by a shadow attribute here.
 - Datomic's Pull can not be exposed to external world, since it lacks feature such as `:no-wildcard`, `:shadow`, `:stealth`, which leaks the implementation details.


You can use this library as an endpoint of a web service for any of your Datomic entities, providing pull options specified:

   - disable wildcard by `:no-wildcard? true`
   - disable ref lookup by `:no-ref? true`
   - hide internal attributes by adding them into `:stealth`
   - use `:shadow` to introduce calculated attributes

If you decide to do so, I suggest:

 - return entities from Datomic queries rather than pull expressions in query only when you use the value internally.

## Map-like data to be pulled

The library not only support pull `java.util.Map` instance, it also support map-like data structures, like `datomic.Entity` (since it is an important scenario, the library has built-in support for it). You an adapt `juxt.pull.protocol/Findable` protocol to any data type.

## References

- David Nolen's Euro Clojure talk in 2014 in Krakow.
- Datomic Pull API: http://docs.datomic.com/pull.html
- Om Next Query Expressions: https://github.com/omcljs/om

## Copyright & License

The MIT License (MIT)

Copyright © 2018 Luo Tian

Copyright © 2016 JUXT LTD.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
