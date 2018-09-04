(defproject robertluo/pull "0.2.2"
  :description "Trees from tables"
  :min-lein-version "2.7.0"
  :url "http://github.com/juxt/pull"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]]
  :profiles
  {:dev
   {:dependencies [[com.datomic/datomic-free "0.9.5697"]]}})
