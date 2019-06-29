(ns xogos.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [xogos.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
