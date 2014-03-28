(ns com.climate.astro-algo.test.date-utils
  "Test functions for converting dates and times relevant to astronomical computation
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998."
  (:require
    [com.climate.astro-algo.date-utils :as du]
    [clj-time.core :refer [date-time]])
  (:use [clojure.test]))

(deftest test-day-of-year
  (testing "test day-of-year in common year"
    ; [1] Example 7.f
    (is (= (du/day-of-year (date-time 1978 11 14)) 318)))
  (testing "test day-of-year in leap year"
    ; [1] Example 7.g
    (is (= (du/day-of-year (date-time 1988 4 22)) 113))))

(deftest decimal-day
  (testing "test decimal-day"
    (is (= (du/decimal-day (date-time 2013 12 25 6)) 25.25))))

(deftest test-julian-day
  (testing "test julian-day from a string"
    (is (= (du/julian-day "2013-12-21T12:34:56.789Z") 2456648.024268391)))
  (testing "test julian-day from a date-time"
    (is (= (du/julian-day (date-time 2013 12 21 12 34 56 789)) 2456648.024268391))))

(deftest test-centuries-since-j2000
  (testing "test centuries-since-j2000"
    ; [1] Example 25.a
    (is (= (format "%.9f" (du/centuries-since-j2000 (date-time 1992 10 13)))
           "-0.072183436"))))
