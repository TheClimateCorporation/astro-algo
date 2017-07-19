# astro-algo
## A Clojure library designed to implement computational methods described in Jean Meeus' _Astronomical Algorithms_.

The astro-algo library provides a celestial Body protocol with interfaces for equatorial
coordinates and standard altitude. A single implementation of the Body protocol is
provided for the Sun. The library also provides a public function to get local coordinates
for a Body at any time and place and a public function to get passages (rising, transit,
setting) for a Body on any day at any location.

The solar position computations implemented in this library are considered low accuracy.
They account for some effects of nutation with simplifications described in AA Ch. 25.
These calculations should be accurate to within 0.01 degrees of _geometric_ position,
however larger differences may be observed due to atmospheric refraction, which is not
accounted for other than the standard values for the effect of atmospheric refraction
at the horizon used in the calculation of passage times.

Passages are calculated according to AA Ch. 15 by interpolating the time that the
celestial body crosses the local meridian (for transit) or the standard altitude (for
rising and setting). Standard altitudes for rising and setting are taken from the
celestial body unless the passages for twilight are desired, in which case the standard
altitude for the specified twilight definition is used.

The library also includes date utils with a reduction of time scales table that spans from
1890 to 2020.

[![Clojars Project](https://img.shields.io/clojars/v/astro-algo.svg)](https://clojars.org/astro-algo)

## Usage

    (:require
      [com.climate.astro-algo.bodies :refer [local-coordinates passages]]
      [clj-time.core :as ct])
    (:import
      [com.climate.astro_algo.bodies Sun])

    (let [sun (Sun.)]
      ; get cosine angle of the sun
      ; on 10/18/1989 00:04:00 (UTC) in San Francisco
      (let [dt (ct/date-time 1989 10 18 0 4 0)
            lon -122.42 ; [degrees east of Greenwich]
            lat 37.77   ; [degrees north of the Equator]
            ; get azimuth and altitude in radians
            {:keys [azimuth altitude]} (local-coordinates sun dt lon lat)]
        ; get the cosine of the zenith (the sin of the altitude)
        ; negative value indicates the sun is geometrically beneath the horizon
        (Math/sin altitude)))

    (let [sun (Sun.)]
      ; get civil daylength on 12/21/2013 at Stonehenge
      (let [date (ct/local-date 2012 12 21)
            lon -1.8262 ; [degrees east of Greenwich]
            lat 51.1788 ; [degrees north of the Equator]
            {:keys [rising transit setting]} (passages sun date lon lat
                                                       ; can be none, civil, astronomical, nautical
                                                       ; none is default
                                                       :include-twilight "civil"
                                                       ; set lower precision (degrees)
                                                       ; to speed up computation
                                                       ; 0.1 is default
                                                       :precision 1.0)]
        ; calculate daylength in hours
        (-> (ct/interval rising setting)
          ct/in-millis ; [milliseconds]
          (/ 3600000)  ; [hours]
          double)))

## License

Copyright © 2017 The Climate Corporation. Distributed under the Apache
License, Version 2.0.  You may not use this library except in compliance with
the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

See the NOTICE file distributed with this work for additional information
regarding copyright ownership.  Unless required by applicable law or agreed
to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied.  See the License for the specific language governing permissions
and limitations under the License.
