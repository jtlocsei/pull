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
             (count (:vhosts (pull data [{:vhosts ['*]}]))))))))

(deftest sequential-map-attributes-test
  (testing "from many attributes"
    (is (= {:docs [{:name "hello"} {:name "ok"}]}
           (pull {:docs [{:name "hello"} {:name "ok"}]}
                 [{:docs [:name]}])))))

(deftest no-wildcard?-option-test
  (testing "no-wildcard? will not pull any attributes"
    (is (= {:name "pull" :docs [{} {}]}
           (pull {:name "pull" :docs [{:name "ok"} {:name "hello"}]}
                 [:name {:docs '[*]}]
                 {:no-wildcard? true})))))

(deftest shadow-attributes-test
  (testing "shadow attributes"
    (is (= {:docs [{:name-len 5} {:name-len 2}]}
           (pull {:docs [{:name "smart"} {:name "ok"}]}
                 [{:docs [:name-len]}]
                 {:shadow {:name-len #(count (:name %))}}))))

  (testing "deep shadow attributes"
    (is (= {:tags [{:name "foo"} {:name "bar"}]}
           (pull {}
                 [{:tags [:name]}]
                 {:shadow
                  {:tags
                   (fn [_]
                     [{:name "foo" :value "none"}
                      {:name "bar"}])}})))))

(deftest stealth-attibutes-test
  (testing "stealth"
    (is (= {:docs [{:author "juxt"}
                   {:author "renewdoit"}]}
           (pull {:docs [{:author "juxt" :password "secret"} {:author "renewdoit"}]}
                 [{:docs [:author :password]}]
                 {:stealth #{:password}})))))

