(ns juxt.pull.protocol
  "Protocols to support map-like pull.

  Clojure's find function are defined on fixed set of classes (java.util.Map),
  but there are map like structures which does not implements Map interface,
  such as datomic.Entity. To support these, you need adapt Findable protocol,
  to support wildcard, adapt WildcardSupport protocol")

(defprotocol Findable
  (-find [o k]
    "Returns a pair of key, value of key k"))

(defprotocol WildcardSupport
  (-keys [o]
    "Returns all keys of object o"))

(extend-type java.util.Map
  WildcardSupport
  (-keys [o]
    (.keySet o))
  Findable
  (-find [o k]
    (find o k)))

(try
  (import '[datomic Entity])
  (extend-type datomic.Entity
    WildcardSupport
    (-keys [o]
      (map keyword (.keySet o)))
    Findable
    (-find [o k]
      [k (.get o k)]))
  (catch ClassNotFoundException _))
