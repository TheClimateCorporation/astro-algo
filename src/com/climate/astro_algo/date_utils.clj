(ns com.climate.astro-algo.date-utils
  "Functions for converting dates and times relevant to astronomical computation
  References:
  [1] Meeus, Jean. _Astronomical Algorithms_ 2nd Ed. Willman-Bell, Inc. 1998."
  (:require
    [clj-time.core :as ct]
    [clj-time.coerce :refer [to-date-time]]
    [com.climate.astro-algo.delta-t :refer [delta-t]])
  (:import [org.joda.time DateTimeConstants]))

(defn ut->td
  "Convert date string or org.joda.time.DateTime to Dynamical Time"
  [dt]
  (let [dt (to-date-time dt)]
    (ct/plus dt (delta-t dt))))

(defn td->ut
  "Convert Dynamical Time String or org.joda.time.DateTime to UTC"
  [dt]
  (let [dt (to-date-time dt)]
    (ct/minus dt (delta-t dt))))

(defn- leap-year?
  "Returns true if given year is a leap year
  [1] Ch.7"
  [year]
  (= (mod year 4) 0))

(defn day-of-year
  "Get integer day of year from org.joda.time.DateTime
  [1] Ch.7"
  [dt]
  (.getDayOfYear ^org.joda.time.DateTime dt))

(defn decimal-day
  "Get decimal day of month from org.joda.time.DateTime"
  [dt]
  ; get fraction of day
  (-> (ct/interval (ct/date-midnight (ct/year dt) (ct/month dt) (ct/day dt)) dt)
    ct/in-millis
    (/ DateTimeConstants/MILLIS_PER_DAY)
    ; add month day to get decimal day
    (+ (ct/day dt))
    double))

(defn julian-day
  "Convert date string or org.joda.time.DateTime to Julian Day
  [1] Eqn (7.1)"
  [dt]
  (let [dt (to-date-time dt)
        [y m] (if (> (ct/month dt) 2)
                [(ct/year dt) (ct/month dt)]
                [(- (ct/year dt) 1) (+ (ct/month dt) 12)])
        a (int (/ y 100))
        b (-> a (/ 4) int (+ (- 2 a)))]
    (+ (-> (+ y 4716)
         (* 365.25)
         int)
       (-> (+ m 1)
         (* 30.6)
         int)
       (decimal-day dt)
       b
       (- 1524.5))))

(defn centuries-since-j2000
  "Convert date string or org.joda.time.DateTime to centuries since J2000.0
  [1] Eqn (12.1)"
  [dt]
  (-> dt
    julian-day
    (- 2451545)
    (/ 36525)))
