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

(declare pull)

(defn local-find
  [shadow local k]
  (if-let [shadow-fn (get shadow k)]
    [k (shadow-fn local)]
    (find local k)))

(defn join-prop
  [prop local global opts]
  (let [[k q] (first prop)
        v     (second (local-find (:shadow opts) local k))]
    (if (and (sequential? v)
             (every? map? v))
      {k (mapv #(pull global % q opts) v)}
      [k (pull global v q opts)])))

(defn pull
  ([global local query {:keys [shadow stealth no-wildcard?] :as opts}]
   (reduce (fn [acc prop]
             (cond
               (= '* prop)
               (if no-wildcard?
                 acc
                 (merge acc
                        (reduce-kv
                         (fn [acc k v]
                           (assoc acc k (denormalize v global))) {} local)))

               (or (string? prop) (keyword? prop))
               (if-not (get stealth prop)
                 (let [kv (local-find shadow local prop)]
                   (conj acc (denormalize kv global)))
                 acc)

               (join? prop)
               (conj acc (join-prop prop local global opts))
               :otherwise acc))
           {}
           query)))
