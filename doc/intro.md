# Introduction to astro-algo

Astro-Algo is a Clojure library designed to implement computational methods described in
_Astronomical Algorithms_ by Jean Meeus. In this release, the following is complete:

 - date utils with reduction of time scales table from 1890 to 2016
 - celestial Body protocol to get equatorial coordinates and standard altitude
 - implementation of Body protocol for the Sun
 - function to get local coordinates for a body
 - function to get passages (rising, transit, setting) for a body

The solar position computations implemented in this library are considered low accuracy.
They account for some effects of nutation with simplifications described in AA Ch. 25.
These calculations should be accurate to within 0.01 degrees of /geometric position/,
however larger differences may be observed due to atmospheric refraction, which is not
accounted for other than the standard values for the effect of atmospheric refraction
at the horizon used in the calculation of passage times.

Passages are calculated according to AA Ch. 15 by interpolating the time that the
celestial body crosses the local meridian (for transit) or the standard altitude (for
rising and setting). Standard altitudes for rising and setting are taken from the
celestial body unless the passages for twilight are desired, in which case the standard
altitude for the specified twilight definition is used.

# API

## com.climate.astro-algo.bodies

### protocol Body

#### equatorial-coordinates

    Calculate apparent right ascension and declination of a body.
    Input:
      this (Body instance)
      dt (org.joda.time.DateTime UTC)
    Returns a map with keys:
      :right-ascension (radians west of Greenwich)
      :declination (radians north of the celestial equator)

#### standard-altitude

    Returns the "standard" altitude, i.e. the geometric altitude of the
    center of the body at the time of apparent rising or setting
    Input:
      this (Body instance)
    Returns standard altitude (degrees)

### local-coordinates

    Calculate local coordinates of a celestial body
    Input:
      body (Body instance)
      dt (org.joda.time.DateTime UTC)
      lon - longitude (degrees East of Greenwich)
      lat - latitude (degrees North of the Equator)
    Returns a map with keys:
      :azimuth (radians westward from the South)
      :altitude (radians above the horizon)

### passages

    Calculate the UTC times of rising, transit and setting of a celestial body
    Input:
      body (Body instance)
      date (org.joda.time.LocalDate)
      lon - longitude (degrees East of Greenwich)
      lat - latitude (degrees North of the Equator)
      opts
        :include-twilight (one of "civil" "astronomical" "nautical" default nil)
        :precision - iterate until corrections are below this (degrees, default 0.1)
    Returns a map with keys:
      :rising (org.joda.time.DateTime UTC)
      :transit (org.joda.time.DateTime UTC)
      :setting (org.joda.time.DateTime UTC)

## com.climate.astro-algo.compute

### nutation-in-longitude

    Calculate the longitude of the ascending node of the Moon's orbit
    on the ecliptic, measured from the mean equinox of the date
    Input: t (centuries since J2000.0)
    Returns: longitude (radians)

### nutation-in-obliquity

    Calculate the nutation in obliquity of the ecliptic
    Input: t (centuries since J2000.0)
    Returns: longitude (degrees)

### true-obliquity

    Calculate the true obliquity of the ecliptic
    Input: t (centuries since J2000.0)
    Returns: true obliquity (degrees)

### mean-sidereal-time

    Calculate mean sidereal time at Greenwich
    Input: dt (org.joda.time.DateTime)
    Returns: sidereal time (degrees)

### apparent-sidereal-time

    Calculate apparent sidereal time at Greenwich
    Input: dt (org.joda.time.DateTime)
    Returns: sidereal time (degrees)

## com.climate.astro-algo.conversions

### deg->rad

    Convert degrees to radians

### rad->deg

    Convert radians to degrees

### instant->deg

    Convert hours, minutes, seconds to degrees

### angle->deg

    Convert degrees, arcminutes, arcseconds to degrees

## com.climate.astro-algo.date-utils

### td->ut

    Convert date string or org.joda.time.DateTime to Dynamical Time

### ut->td

    Convert Dynamical Time String or org.joda.time.DateTime to UTC

### day-of-year

    Get integer day of year from org.joda.time.DateTime
    [1] Ch.7

### decimal-day

    Get decimal day of month from org.joda.time.DateTime

### julian-day

    Convert date string or org.joda.time.DateTime to Julian Day
    [1] Eqn (7.1)

### centuries-since-j2000

    Convert date string or org.joda.time.DateTime to centuries since J2000.0
    [1] Eqn (12.1)

## com.climate.astro-algo.delta-t

### delta-t

    Get difference between dynamical time and universal time (delta_T = TD - UT)
    Input: org.joda.time.DateTime
    Returns: org.joda.time.Period

