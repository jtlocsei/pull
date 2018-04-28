(ns juxt.pull.core
  (:require
   [juxt.pull.core.impl :as impl]))

(defn pull
  "Take a query, and some state. Return a map."
  ([data query]
   (pull data query nil))
  ([data query shadow]
   (impl/pull data data query shadow)))
