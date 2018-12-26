;; Copyright © 2016, JUXT LTD.

(ns juxt.pull.core-test
  (:require
   [clojure.test :refer :all]
   [juxt.pull.core :refer :all]
   [juxt.pull.spec]
   [orchestra.spec.test :as stest]))

(use-fixtures :once (fn [f] (stest/instrument) (f) (stest/unstrument)))

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

    (testing "Specify :no-ref option should not lookup ref"
      (is (= {:server {:port 8080, :vhosts [[:vhosts "http://localhost:8080"]]}}
             (pull data [:server] {:no-ref? true}))))

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
                 {:no-wildcard? true}))))
  (testing "for plain map attributes, no-wildcard? also prevent their pulling"
    (is (= {:docs [{} {}]}
           (pull {:docs [{:name "ok"} {:name "hello"}]}
                 [:docs]
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

(deftest pull-in-set
  (testing "Set value can also be pulled"
    (is (= {:docs [{:author "bar"} {:author "foo"}]}
           (pull {:docs #{{:author "foo"} {:author "bar"}}}
                 [{:docs [:author]}])))))

(deftest pull-datomic-entity
  (testing "Datomic's Entity are also supported"
    (let [entity (fn [m]
                   (reify datomic.Entity
                     (keySet [_]
                       (.keySet m))
                     (get [_ k]
                       (.get m k))))
          ent    {:docs [(entity {:author "foo"}) (entity {:author :bar})]}]
      (is (= {:docs [{:author "foo"} {:author :bar}]}
             (pull ent [{:docs [:author]}]))))))

(deftest pull-on-nil-values
  (testing "For nil valued attribute should pulled as nil"
    (is (= {:a nil} (pull {:a nil} [:a :b])))))

(deftest pull-on-nil
  (testing "Pull on nil just returns nil"
    (is (nil? (pull nil [:a])))))

(deftest pull-not-add-nil-key
  (testing "Pull on map collections should not add nil key"
    (is (= {:a 1
            :c [{:type :wuxing :amount 100}
                {:type :seed :seed {:id "111"}}]}
           (pull {:a 1
                  :c [{:type   :wuxing
                       :amount 100}
                      {:type :seed
                       :seed {:id "111"}}]}
                 [:a {:c [:type :amount
                          {:seed [:id]}]}])))))
