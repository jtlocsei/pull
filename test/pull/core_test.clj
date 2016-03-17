;; Copyright © 2016, JUXT LTD.

(ns pull.core-test
  (:require
   [clojure.test :refer :all]
   [pull.core :refer :all]))

(deftest pull-test
  (testing "basic pull"
    (let [data {:name "pull"
                :routes {:main ["/" :abc]}
                :vhosts {"http://localhost:8080" ^:ref [:routes :main]}
                :server {:port 8080
                         :vhosts [^:ref [:vhosts "http://localhost:8080"]]}}]
      
      (is (= {:name "pull"} (pull [:name] data)))
      (is (= {:name "pull" :vhosts {"http://localhost:8080" ["/" :abc]}}
             (pull [:name :vhosts] data))))))

(pull
 [:name {:server [:port]}]

 {:name "pull"
  :routes {:main ["/" :abc]}
  :vhosts {"http://localhost:8080" ^:ref [:routes :main]}
  :server {:port 8080
           :vhosts [^:ref [:vhosts "http://localhost:8080"]]}}
      

 )
