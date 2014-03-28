(ns com.climate.astro-algo.test.compute
  "Test functions to calculate local coordinates and passages
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998."
  (:require
    [com.climate.astro-algo.compute :as compute]
    [com.climate.astro-algo.bodies :refer [equatorial-coordinates]]
    [com.climate.astro-algo.conversions :as conv]
    [com.climate.astro-algo.date-utils :as du]
    [clj-time.core :refer [date-time]])
  (:import [com.climate.astro_algo.bodies Sun])
  (:use [clojure.test]))

(deftest test-nutation-in-longitude
  (testing "test nutation-in-longitude"
    ; from [1] Example 22.a
    ; expected values are not exact due to simplifications in the computation
    (let [t (du/centuries-since-j2000 (date-time 1987 4 10))
          actual (compute/nutation-in-longitude t)
          expected (conv/angle->deg 0 0 -3.778)]
      (is (= (format "%.3f" actual) (format "%.3f" expected))))))

(deftest test-nutation-in-obliquity
  (testing "test nutation-in-obliquity"
    ; from [1] Example 22.a
    ; expected values are not exact due to simplifications in the computation
    (let [t (du/centuries-since-j2000 (date-time 1987 4 10))
          actual (compute/nutation-in-obliquity t)
          expected (conv/angle->deg 0 0 9.443)]
      (is (= (format "%.3f" actual) (format "%.3f" expected))))))

(deftest test-true-obliquity
  (testing "test true-obliquity vs. NOAA"
    ; test values extracted from http://www.esrl.noaa.gov/gmd/grad/solcalc/
    ; to get more precision, you have to plug in to the javascript console
    ; after setting the date in the form (we'll plug in the time ourselves)
    ; IMPORTANT: NOAA does not convert the time entered to dynamical time
    ;            so you have to add in the delta T yourself,
    ;            in this case 56.3 seconds for the year 1989
    ; > var jday = getJD()
    ; > var total = jday + ((4.0 * 60) + 56.3) / (60 * 60 * 24)
    ; > var T = calcTimeJulianCent(total)
    ; > calcObliquityCorrection(T)
    ; 23.442647168392085
    (let [t (du/centuries-since-j2000 (du/ut->td (date-time 1989 10 18 0 4 0)))]
      ; this precision is way more than the accuracy of the algorithm
      ; but it's fun to see that we are within a trillionth of a degree
      (is (= (format "%.12f" (compute/true-obliquity t)) "23.442647168392"))))
  (testing "test true-obliquity vs. AA"
    ; from [1] Example 22.a
    ; expected values are not exact due to simplifications in the computation
    (let [t (du/centuries-since-j2000 (date-time 1987 4 10))
          actual (compute/true-obliquity t)
          expected (conv/angle->deg 23 26 36.85)]
      (is (= (format "%.2f" actual) (format "%.2f" expected))))))

(deftest test-mean-sidereal-time
  (testing "test mean-sidereal-time"
    ; from [1] Example 12.1
    ; expected values are not exact due to simplifications in the computation
    (let [dt (date-time 1987 4 10)
          actual (compute/mean-sidereal-time dt)
          expected (conv/instant->deg 13 10 46.3668)]
      (is (= (format "%.3f" actual) (format "%.3f" expected))))))

(deftest test-apparent-sidereal-time
  (testing "test apparent-sidereal-time"
    ; from [1] Example 12.1
    ; expected values are not exact due to simplifications in the computation
    (let [dt (date-time 1987 4 10)
          actual (compute/apparent-sidereal-time dt)
          expected (conv/instant->deg 13 10 46.1351)]
      (is (= (format "%.3f" actual) (format "%.3f" expected))))))
