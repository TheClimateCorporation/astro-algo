(ns com.climate.astro-algo.compute
  "Functions to calculate local coordinates and passages
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998."
  (:require
    [clj-time.core :as ct]
    [com.climate.astro-algo.date-utils :as du]
    [com.climate.astro-algo.conversions :as conv]))

(defn- nutation-correction-factor
  "Calculate the longitude of the ascending node of the Moon's orbit
  on the ecliptic, measured from the mean equinox of the date
  Input: t (centuries since J2000.0)
  Returns: longitude (radians)"
  [t]
  ; simplified from [1] Ch.22
  (-> (- 125.04 (* 1934.136 t))
    conv/deg->rad))

(defn nutation-in-longitude
  "Calculate the nutation in longitude
  Input: t (centuries since J2000.0)
  Returns: longitude (degrees)"
  [t]
  ; simplified from [1] Ch.22
  (* -0.00478 (Math/sin (nutation-correction-factor t))))

(defn nutation-in-obliquity
  "Calculate the nutation in obliquity of the ecliptic
  Input: t (centuries since J2000.0)
  Returns: longitude (degrees)"
  [t]
  ; simplified from [1] Ch.22
  (* 0.00256 (Math/cos (nutation-correction-factor t))))

(defn true-obliquity
  "Calculate the true obliquity of the ecliptic
  Input: t (centuries since J2000.0)
  Returns: true obliquity (degrees)"
  [t]
  ; since IAU spec uses degrees, arcminutes, arcseconds
  ; first compute arcseconds component
  (let [mean-obliquity (-> (+ 21.448
                             (* -46.8150 t)
                             (* -0.00059 (Math/pow t 2))
                             (* 0.001813 (Math/pow t 3)))
                         ; then add in degrees & arcminutes
                         (#(conv/angle->deg 23 26 %)))] ; [1] Eqn (22.2)
    (+ mean-obliquity (nutation-in-obliquity t))))

(defn mean-sidereal-time
  "Calculate mean sidereal time at Greenwich
  Input: dt (org.joda.time.DateTime)
  Returns: sidereal time (degrees)"
  [dt]
  (let [jd (du/julian-day dt)
        t (du/centuries-since-j2000 dt)]
    (-> (+ 280.46061837
          (* 360.98564736629
             (- jd 2451545))
          (* 0.000387933
             (Math/pow t 2))
          (/ (Math/pow t 3)
             -38710000)) ; [1] Eqn (12.4)
      (mod 360))))

(defn apparent-sidereal-time
  "Calculate apparent sidereal time at Greenwich
  Input: dt (org.joda.time.DateTime)
  Returns: sidereal time (degrees)"
  [dt]
  (let [t (du/centuries-since-j2000 dt)]
    (-> (+ (mean-sidereal-time dt)
           (* (nutation-in-longitude t)
              (Math/cos (conv/deg->rad (true-obliquity t)))))
      (mod 360))))
