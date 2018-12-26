;; Copyright © 2018, Luo Tian.
;; Copyright © 2016, JUXT LTD.

(ns ^:no-doc juxt.pull.core.impl
  (:require
   [juxt.pull.protocol :as p]
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

(def findable? (partial satisfies? p/Findable))

(defn local-find
  [local k {:keys [shadow stealth]}]
  (when (not (get stealth k))
    (if-let [shadow-fn (get shadow k)]
      [k (shadow-fn local)]
      (p/-find local k))))

(defn all-findable? [v]
  (and v (seqable? v) (every? findable? v)))

(defn pullv
  [global [k v] q opts]
  (if (all-findable? v)
      {k (mapv #(pull global % q opts) v)}
      {k (pull global v q opts)}))

(defn join-prop
  [prop local global opts]
  (let [[k q] (first prop)]
    (pullv global (local-find local k opts) q opts)))

(defn wildcard
  [global local opts]
  (pull global local (vec (p/-keys local)) opts))

(defn key? [k]
  (or (string? k) (keyword? k)))

(defn pull
  ([global local query {:keys [no-wildcard? no-ref?] :as opts}]
   (when local
     (reduce (fn [acc prop]
               (cond
                 (= '* prop)
                 (if no-wildcard?
                   acc
                   (merge acc (wildcard global local opts)))

                 (key? prop)
                 (if-let [[k v :as kv] (local-find local prop opts)]
                   (conj acc
                         (if (all-findable? v)
                           (pullv local kv '[*] opts)
                           (if no-ref?
                             kv
                             (denormalize kv global))))
                   acc)

                 (join? prop)
                 (let [joined-prop (join-prop prop local global opts)]
                   (if (= (ffirst joined-prop) (ffirst prop))
                     (conj acc joined-prop)
                     acc))
                 :otherwise acc))
             {}
             query))))
