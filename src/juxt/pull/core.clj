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
  ([global local query]
   (reduce (fn [acc prop]
             (cond
               (= '* prop)
               (merge acc
                      (reduce-kv (fn [acc k v] (assoc acc k (denormalize v global))) {} local))

               (or (string? prop) (keyword? prop))
               (conj acc (denormalize (find local prop) global))

               (join? prop)
               (conj acc (let [[k q] (first prop)]
                           (let [v (get local k)]
                             (if (and (sequential? v)
                                      (every? map? v))
                               {k (mapv #(pull global % q) v)}
                               [k (pull global v q)]))))
               :otherwise acc))
           {}
           query))

  ([local query]
   (pull local local query)))
