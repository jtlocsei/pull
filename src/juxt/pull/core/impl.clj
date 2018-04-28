;; Copyright © 2016, JUXT LTD.

(ns juxt.pull.core.impl
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
  ([global local query {:keys [shadow stealth] :as opts}]
   (reduce (fn [acc prop]
             (cond
               (= '* prop)
               (merge acc
                      (reduce-kv (fn [acc k v] (assoc acc k (denormalize v global))) {} local))

               (or (string? prop) (keyword? prop))
               (if-not (get stealth prop)
                 (if-let [shadow-fn (get shadow prop)]
                   (conj acc [prop (shadow-fn local)])
                   (conj acc (denormalize (find local prop) global)))
                 acc)

               (join? prop)
               (conj acc (let [[k q] (first prop)]
                           (let [v (get local k)]
                             (if (and (sequential? v)
                                      (every? map? v))
                               {k (mapv #(pull global % q opts) v)}
                               [k (pull global v q opts)]))))
               :otherwise acc))
           {}
           query)))
