;; Copyright © 2016, JUXT LTD.

(ns juxt.pull.core
  (:require
   [clojure.walk :refer [postwalk]]))

(defn- denormalize*
  [v state]
  (if (:ref (meta v))
    (postwalk #(denormalize* % state) (get-in state v))
    v))

(defn denormalize
  "Take a value, and some state. Walk the value, replacing any idents
  by looking up their values in the state. Do this recursively to
  create a tree."
  [v state]
  (postwalk #(denormalize* % state) v))

(defn join? [p]
  (and (map? p) (= (count p) 1)))

(defn pull
  "Take a query, and some state. Return a map."
  [query state]
  (reduce (fn [acc prop]
            (cond
              (keyword? prop) (conj acc (denormalize (find state prop) state))
              (join? prop) (conj acc (let [[k q] (first prop)]
                                       [k (pull q (get state k))]))
              :otherwise acc))
          {}
          query))







