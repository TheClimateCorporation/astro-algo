(ns com.climate.astro-algo.bodies
  "Protocol, classes and functions for celestial bodies
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998."
  (:require
    [clj-time.core :as ct]
    [clj-time.coerce :refer [to-date-time]]
    [com.climate.astro-algo.delta-t :refer [delta-t]]
    [com.climate.astro-algo.date-utils :as du]
    [com.climate.astro-algo.conversions :as conv]
    [com.climate.astro-algo.compute :as compute])
  (:import [org.joda.time DateTimeConstants]))

;; celestial bodies
(defprotocol Body
  "Protocol for a celestial body"
  (equatorial-coordinates
    [this dt]
    "Calculate apparent right ascension and declination of a body.
    Input:
      this (Body instance)
      dt (org.joda.time.DateTime UTC)
    Returns a map with keys:
      :right-ascension (radians west of Greenwich)
      :declination (radians north of the celestial equator)")
  (standard-altitude
    [this]
    "Returns the \"standard\" altitude, i.e. the geometric altitude of the
    center of the body at the time of apparent rising or setting
    Input:
      this (Body instance)
    Returns standard altitude (degrees)"))

(defrecord Sun []
  Body
  (equatorial-coordinates
    [this dt]
    ; this implements the low-accuracy equations from [1]
    ; which provide accuracy within 0.01 degrees without
    ; any correction for nutation.
    ; in this case, correction for nutation is undergone
    ; with the simplifications provided in [1] Ch.25
    (let [t (-> dt du/ut->td du/centuries-since-j2000)
          ;; mean equinox of the date, degrees
          l_0 (-> (+ 280.46646
                    (* 36000.76983 t)
                    (* 0.0003032 (Math/pow t 2))) ; [1] Eqn (25.2)
                (mod 360))
          ; mean anomoly, degrees
          m (+ 357.52911
               (* 35999.05029 t)
               (* -0.0001537 (Math/pow t 2))) ; [1] Eqn (25.3)
          ;; Sun's equation of center, degrees
          c (+ (* (- 1.914602
                     (* 0.004817 t)
                     (* 0.000014 (Math/pow t 2)))
                  (Math/sin (conv/deg->rad m)))
               (* (- 0.019993
                     (* 0.000101 t))
                  (Math/sin (* 2 (conv/deg->rad m))))
               (* 0.000289 (Math/sin (* 3 (conv/deg->rad m)))))
          ;; Sun's true longitude, degrees
          o (+ l_0 c)
          ;; Sun's apparent longitude, degrees
          lambda (+ o -0.00569 (compute/nutation-in-longitude t))
          ;; true obliquity of the ecliptic, degrees
          e (compute/true-obliquity t)
          ;; right ascension, radians
          ra (Math/atan2 (* (Math/cos (conv/deg->rad e))
                            (Math/sin (conv/deg->rad lambda)))
                         (Math/cos (conv/deg->rad lambda))) ; [1] Eqn (25.6)
          ;; declination, radians
          decl (Math/asin (* (Math/sin (conv/deg->rad e))
                             (Math/sin (conv/deg->rad lambda))))] ; [1] Eqn (25.7)
      {:right-ascension ra
       :declination decl}))
  (standard-altitude
    [this]
    -5/6)) ; [1] Ch.15

; functions for celestial bodies
(defn local-coordinates
  "Calculate local coordinates of a celestial body
  Input:
    body (Body instance)
    dt (org.joda.time.DateTime UTC)
    lon - longitude (degrees East of Greenwich)
    lat - latitude (degrees North of the Equator)
  Returns a map with keys:
    :azimuth (radians westward from the South)
    :altitude (radians above the horizon)"
  [body dt lon lat]
  (let [{:keys [right-ascension declination]} (equatorial-coordinates body dt)
        lon (conv/deg->rad lon)
        lat (conv/deg->rad lat)
        theta_0 (-> dt du/ut->td compute/apparent-sidereal-time conv/deg->rad)
        hour-angle (- theta_0 (- lon) right-ascension) ; [1] Ch.13
        azimuth (Math/atan2 (Math/sin hour-angle)
                            (- (* (Math/cos hour-angle)
                                  (Math/sin lat))
                               (* (Math/tan declination)
                                  (Math/cos lat)))) ; [1] Eqn (13.5)
        altitude (Math/asin (+ (* (Math/sin lat)
                                  (Math/sin declination))
                               (* (Math/cos lat)
                                  (Math/cos declination)
                                  (Math/cos hour-angle))))] ; [1] Eqn (13.6)
    {:azimuth azimuth
     :altitude altitude}))

(defn- interpolate
  "Function for interpolation based on [1] Eqn (3.3)"
  [v0 v1 v2 n]
  (let [a (- v1 v0)
        b (- v2 v1)
        c (- b a)]
    (+ v1 (/ (* n (+ a b (* n c))) 2))))

(defn passages
  "Calculate the UTC times of rising, transit and setting of a celestial body
  Input:
    body (Body instance)
    date (org.joda.time.LocalDate)
    lon - longitude (degrees East of Greenwich)
    lat - latitude (degrees North of the Equator)
    opts
      :include-twilight (one of \"civil\" \"astronomical\" \"nautical\" default nil)
      :precision - iterate until corrections are below this (degrees, default 0.1)
  Returns a map with keys:
    :rising (org.joda.time.DateTime UTC)
    :transit (org.joda.time.DateTime UTC)
    :setting (org.joda.time.DateTime UTC)"
  [body date lon lat & {:keys [include-twilight precision]
                :or {precision 0.1}}]
  (let [lat (conv/deg->rad lat)
        ; adjust standard altitude to include twilight if desired
        std-alt (condp = include-twilight
                  ; twilight standard altitude values from
                  ; http://aa.usno.navy.mil/faq/docs/RST_defs.php
                  "civil" -6.0
                  "nautical" -12.0
                  "astronomical" -18.0
                  (standard-altitude body))
        dt (to-date-time date)
        ;; apparent sidereal time at 0h UT, degrees
        theta_0 (compute/apparent-sidereal-time dt)
        ;; get three days of equatorial coordinates at 0h TD
        coords (map #(equatorial-coordinates body %) [(ct/minus dt (ct/days 1))
                                                      dt
                                                      (ct/plus dt (ct/days 1))])
        ;; get right ascensions, degrees
        [ra1 ra2 ra3] (map #(-> % :right-ascension conv/rad->deg) coords)
        ;; tweak right ascensions around 180 degree boundaries after conversion
        ;; this is needed since we are to interpolate the three values
        ra1 (if (> ra1 ra2) (- ra1 360) ra1)
        ra3 (if (< ra3 ra2) (+ ra3 360) ra3)
        ;; get declinations, radians
        [decl1 decl2 decl3] (map #(:declination %) coords)
        ;; approximate hour angle, degrees [1] Eqn (15.1)
        cos-ha (/ (- (Math/sin (conv/deg->rad std-alt))
                     (* (Math/sin lat) (Math/sin decl2)))
                  (* (Math/cos lat) (Math/cos decl2)))
        ;; force hour angle into range [0 180]
        ha (cond
             (> cos-ha 1) 0
             (< cos-ha -1) (conv/rad->deg (Math/acos -1))
             :default (conv/rad->deg (Math/acos cos-ha)))
        ;; approximate m, fraction of days
        ;; for rising, transit, setting [1] Eqn (15.2)
        ;; take out any day boundaries we crossed
        separate-days (fn [m] (let [m_ (mod m 1)] [m_ (- m_ m)]))
        [m0 d0] (-> (/ (- ra2 lon theta_0) 360) ; transit
                  separate-days)
        separate-days (fn [m] (let [m_ (mod m 1)] [m_ (- m m_)]))
        [m1 d1] (-> (- m0 (/ ha 360)) ; rising
                  separate-days)
        [m2 d2] (-> (+ m0 (/ ha 360)) ; setting
                  separate-days)]
        ;; interpolate right-ascension & declination to adjust m values
        (letfn [(interp-m
                  [idx m]
                  (binding [*warn-on-reflection* true]
                    (let [n (+ m (/ (.getMillis ^org.joda.time.Period (delta-t dt))
                                    DateTimeConstants/MILLIS_PER_DAY))
                          ra (interpolate ra1 ra2 ra3 n)
                          decl (interpolate decl1 decl2 decl3 n)
                          ; apparent sidereal time, degrees [1] Ch.15
                          theta (+ theta_0 (* 360.985647 m))
                          ; local hour angle, degrees [1] Ch.15
                          h (+ theta lon (- ra))]
                      (if (= idx 0)
                        ; transit
                        (/ h -360) ; [1] Ch.15
                        ; rising or setting
                        ; get altitude, degrees [1] Eqn (13.6)
                        (let [alt (-> (Math/asin (+ (* (Math/sin lat)
                                                       (Math/sin decl))
                                                    (* (Math/cos lat)
                                                       (Math/cos decl)
                                                       (Math/cos (conv/deg->rad h)))))
                                    conv/rad->deg)]
                          (/ (- alt std-alt)
                             (* 360 (Math/cos decl) (Math/cos lat)
                                (Math/sin (conv/deg->rad h)))))))))] ; [1] Ch.15
          (let [[m0 m1 m2] (loop [ms [m0 m1 m2]]
                             (let [ms (vec ms)
                                   delta-ms (vec (map-indexed interp-m ms))
                                   new-ms (for [i (range 3)]
                                            (+ (get ms i) (get delta-ms i)))]
                               ;; recur if any delta-ms are outside our precision
                               (if-not (some true? (map #(> (Math/abs ^double %)
                                                            (/ precision 360)) ; deg->days
                                                        delta-ms))
                                 new-ms
                                 (recur new-ms))))
                ;; done! return UTC datetimes
                transit (ct/plus dt (ct/millis (* (+ m0 d0) DateTimeConstants/MILLIS_PER_DAY)))
                rising (ct/plus dt (ct/millis (* (+ m1 d1) DateTimeConstants/MILLIS_PER_DAY)))
                setting (ct/plus dt (ct/millis (* (+ m2 d2) DateTimeConstants/MILLIS_PER_DAY)))]
            {:transit transit
             :rising rising
             :setting setting}))))

