(ns com.climate.astro-algo.test.conversions
  "Test functions for unit conversions
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998."
  (:require [com.climate.astro-algo.conversions :as conv])
  (:use [clojure.test]))

(deftest test-deg->rad
  (testing "test deg->rad"
    (is (= (conv/deg->rad 180) (Math/PI)))))

(deftest test-rad->deg
  (testing "test rad->deg"
    (is (= (conv/rad->deg (Math/PI)) 180.0))))

(deftest test-instant->deg
  (testing "test instant->deg"
    ; from [1] Example 1.a
    (is (= (format "%.5f" (conv/instant->deg 9 14 55.8)) "138.73250"))))

(deftest test-angle->deg
  (testing "test angle->deg"
    ; from [1] Example 1.a
    (is (= (conv/angle->deg 23 26 44) 21101/900))))
