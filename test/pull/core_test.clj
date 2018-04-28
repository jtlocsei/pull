;; Copyright © 2016, JUXT LTD.

(ns juxt.pull.core-test
  (:require
   [clojure.test :refer :all]
   [juxt.pull.core :refer :all]))

(deftest pull-test
  (let [data {:name "pull"
              :routes {:main ["/" :abc]
                       :other ["/" :def]}
              :vhosts {"http://localhost:8080" ^:ref [:routes :main]
                       "https://localhost:8443" ^:ref [:routes :main]
                       "https://localhost:8000" ^:ref [:routes :other]}
              :docs [{:name "hello" :author "juxt" :password "secret"}
                     {:name "ok" :author "renewdoit"}]
              :server {:port 8080
                       :vhosts [^:ref [:vhosts "http://localhost:8080"]]}}]

    (testing "attributes"
      (is (= {:name "pull"} (pull data [:name]))))

    (testing "denormalization"
      (is (= {:server {:port 8080, :vhosts [["/" :abc]]}}
             (pull data [:server]))))

    (testing "maps"
      (is (= {:vhosts {"http://localhost:8080" ["/" :abc]}}
             (pull data [{:vhosts ["http://localhost:8080"]}]))))

    (testing "wildcard"
      (is (= 3
             (count (:vhosts (pull data [{:vhosts ['*]}]))))))

    (testing "from many attributes"
      (is (= {:docs [{:name "hello"} {:name "ok"}]}
             (pull data [{:docs [:name]}]))))

    (testing "apply f for transform attribute"
      (is (= {:docs [{:name-len 5} {:name-len 2}]}
             (pull data [{:docs [:name-len]}]
                   {:shadow {:name-len #(count (:name %))}}))))

    (testing "stealth"
      (is (= {:docs [{:name-len 5 :author "juxt"}
                     {:name-len 2 :author "renewdoit"}]}
             (pull data [{:docs [:name-len :author :password]}]
                   {:shadow {:name-len #(count (:name %))}
                    :stealth #{:password}}))))))
