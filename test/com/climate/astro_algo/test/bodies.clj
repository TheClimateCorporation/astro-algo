(ns com.climate.astro-algo.test.bodies
  "Test functions for celestial bodies
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998."
  (:require
    [com.climate.astro-algo.bodies :refer [equatorial-coordinates
                                           local-coordinates
                                           passages]]
    [com.climate.astro-algo.conversions :as conv]
    [clj-time.core :refer [date-time local-date]])
  (:import [com.climate.astro_algo.bodies Sun])
  (:use [clojure.test]))

(deftest test-equatorial-coordinates-sun
  (testing "test equatorial-coordinates for Sun"
    ; test values extracted from http://www.esrl.noaa.gov/gmd/grad/solcalc/
    ; to get more precision, you have to plug in to the javascript console
    ; after setting the date in the form (we'll plug in the time ourselves)
    ; IMPORTANT: NOAA does not convert the time entered to dynamical time
    ;            so you have to add in the delta T yourself,
    ;            in this case 56.3 seconds for the year 1989
    ; > var jday = getJD()
    ; > var total = jday + ((4.0 * 60) + 56.3) / (60 * 60 * 24)
    ; > var T = calcTimeJulianCent(total)
    ; > calcSunDeclination(T)
    ; -9.531532085192655
    ; > calcSunRtAscension(T)
    ; -157.21821959211204
    (let [sun (Sun.)
          dt (date-time 1989 10 18 0 4 0)
          coords (equatorial-coordinates sun dt)
          decl (-> coords :declination conv/rad->deg)
          ra (-> coords :right-ascension conv/rad->deg)]
      ; this precision is way more than the accuracy of the algorithm
      ; but it's fun to see that we are within a trillionth of a degree
      (is (= (format "%.12f" decl) "-9.531532085193"))
      (is (= (format "%.12f" ra) "-157.218219592112")))))

(deftest test-local-coordinates
  (testing "test local-coordinates"
    ; test values extracted from http://www.esrl.noaa.gov/gmd/grad/solcalc/
    ; expected values are not exact due to differences in methodology,
    ; especially that they do not use dynamical time
    ; and we do not correct for atmospheric refraction
    (let [sun (Sun.)
          dt (date-time 1989 10 18 0 4 0)
          lon -122.42
          lat 37.77
          local-coords (local-coordinates sun dt lon lat)]
      (is (= (format "%.1f" (-> local-coords :azimuth conv/rad->deg))
             (format "%.1f" (-> 244.9 (- 180) (mod 360)))))
      (is (= (format "%.1f" (-> local-coords :altitude conv/rad->deg))
             (format "%.1f" 15.04))))))

(deftest test-passages
  (testing "test passages"
    ; test values verified with http://www.esrl.noaa.gov/gmd/grad/solcalc/
    ; expected values are not exact to NOAA due to differences in methodology,
    ; especially that they do not use dynamical time
    (let [sun (Sun.)
          date (local-date 1989 10 17)
          lon -122.42
          lat 37.77
          passages (passages sun date lon lat)]
      (is (= (-> passages :transit .toString) "1989-10-17T19:55:11.088Z"))
      (is (= (-> passages :rising .toString) "1989-10-17T14:20:09.093Z"))
      (is (= (-> passages :setting .toString) "1989-10-18T01:30:38.175Z")))))

