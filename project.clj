(defproject robertluo/pull "0.2.10"
  :description "Trees from tables"
  :min-lein-version "2.7.0"
  :url "http://github.com/robertluo/pull"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]]
  :profiles
  {:dev
   {:dependencies [[com.datomic/datomic-free "0.9.5697"]
                   [orchestra "2018.08.19-1"]]}})
