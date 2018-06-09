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
  [local k {:keys [shadow stealth]}]
  (when (not (get stealth k))
    (if-let [shadow-fn (get shadow k)]
      [k (shadow-fn local)]
      (find local k))))

(defn all-map? [v]
  (and (sequential? v) (every? map? v)))

(defn pullv
  [global [k v] q opts]
  (if (all-map? v)
      {k (mapv #(pull global % q opts) v)}
      [k (pull global v q opts)]))

(defn join-prop
  [prop local global opts]
  (let [[k q] (first prop)]
    (pullv global (local-find local k opts) q opts)))

(defn wildcard
  [global ^java.util.Map local opts]
  (pull global local (vec (.keySet local)) opts))

(defn pull
  ([global local query {:keys [no-wildcard?] :as opts}]
   (reduce (fn [acc prop]
             (cond
               (= '* prop)
               (if no-wildcard?
                 acc
                 (merge acc (wildcard global local opts)))

               (or (string? prop) (keyword? prop))
               (if-let [[k v :as kv] (local-find local prop opts)]
                 (conj acc
                       (if (or (map? v) (all-map? v))
                         (pullv local kv '[*] opts)
                         (denormalize kv global)))
                 acc)

               (join? prop)
               (conj acc (join-prop prop local global opts))
               :otherwise acc))
           {}
           query)))
