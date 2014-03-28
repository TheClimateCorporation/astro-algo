(ns com.climate.astro-algo.conversions
  "Functions for unit conversions
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998.")

(defn deg->rad
  "Convert degrees to radians"
  [deg]
  ; based on [1] Ch.1
  (* deg (/ (Math/PI) 180)))

(defn rad->deg
  "Convert radians to degrees"
  [rad]
  ; based on [1] Ch.1
  (* rad (/ 180 (Math/PI))))

(defn instant->deg
  "Convert hours, minutes, seconds to degrees"
  ; based on [1] Ch.1
  [hours minutes seconds]
  (let [total-hours (-> (/ seconds 60)
                      (+ minutes)
                      (/ 60)
                      (+ hours))]
    (* total-hours 15)))

(defn angle->deg
  "Convert degrees, arcminutes, arcseconds to degrees"
  ; based on [1] Ch.1
  [degrees arcminutes arcseconds]
  (-> (/ arcseconds 60)
    (+ arcminutes)
    (/ 60)
    (+ degrees)))
