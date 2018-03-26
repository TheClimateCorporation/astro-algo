(ns com.climate.astro-algo.delta-t
  "Get historical and extrapolated values for Delta T"
  (:require
    [clj-time.core :as ct])
  (:use [clojure.tools.logging :only (warn)]))

; Values from _Astronomical Almanac for the Year 2017_ page K9
; by U.S. Nautical Almanac Office
; These are based on terrestrial observations which are
; difficult to predict, so an extrapolation of only a few years is provided
(defonce DELTA-T-MAP {1890 -5.87 1891 -6.01 1892 -6.19 1893 -6.64 1894 -6.44
                      1895 -6.47 1896 -6.09 1897 -5.76 1898 -4.66 1899 -3.74
                      1900 -2.72 1901 -1.54 1902 -0.02 1903  1.24 1904  2.64
                      1905  3.86 1906  5.37 1907  6.14 1908  7.75 1909  9.13
                      1910 10.46 1911 11.53 1912 13.36 1913 14.65 1914 16.01
                      1915 17.20 1916 18.24 1917 19.06 1918 20.25 1919 20.95
                      1920 21.16 1921 22.25 1922 22.41 1923 23.02 1924 23.49
                      1925 23.62 1926 23.86 1927 24.49 1928 24.34 1929 24.08
                      1930 24.02 1931 24.00 1932 23.87 1933 23.95 1934 23.86
                      1935 23.93 1936 23.73 1937 23.92 1938 23.96 1939 24.02
                      1940 24.33 1941 24.83 1942 25.30 1943 25.70 1944 26.24
                      1945 26.77 1946 27.28 1947 27.78 1948 28.25 1949 28.71
                      1950 29.15 1951 29.57 1952 29.97 1953 30.36 1954 30.72
                      1955 31.07 1956 31.35 1957 31.68 1958 32.18 1959 32.68
                      1960 33.15 1961 33.59 1962 34.00 1963 34.47 1964 35.03
                      1965 35.73 1966 36.54 1967 37.43 1968 38.29 1969 39.20
                      1970 40.18 1971 41.17 1972 42.23 1973 43.37 1974 44.49
                      1975 45.48 1976 46.46 1977 47.52 1978 48.53 1979 49.59
                      1980 50.54 1981 51.38 1982 52.17 1983 52.96 1984 53.79
                      1985 54.34 1986 54.87 1987 55.32 1988 55.82 1989 56.30
                      1990 56.86 1991 57.57 1992 58.31 1993 59.12 1994 59.98
                      1995 60.78 1996 61.63 1997 62.29 1998 62.97 1999 63.47
                      2000 63.83 2001 64.09 2002 64.30 2003 64.47 2004 64.57
                      2005 64.69 2006 64.85 2007 65.15 2008 65.46 2009 65.78
                      2010 66.07 2011 66.32 2012 66.60 2013 66.90 2014 67.00
                      2015 68.00 2016 68.00 2017 68.00 2018 69.00 2019 69.00
                      2020 69.00})

(defonce MIN-YEAR (apply min (keys DELTA-T-MAP)))
(defonce MAX-YEAR (apply max (keys DELTA-T-MAP)))

(defn delta-t
  "Get difference between dynamical time and universal time (delta_T = TD - UT)
  Input: org.joda.time.DateTime
  Returns: org.joda.time.Period"
  [dt]
  (let [year (ct/year dt)
        secs (cond
               (< year MIN-YEAR) 0.0
               (> year MAX-YEAR) (do
                                   (warn "Year" year "is not extrapolated"
                                         "in reduction of time scales. Using"
                                         "delta-t value for year" MAX-YEAR)
                                   (get DELTA-T-MAP MAX-YEAR))
               :else (get DELTA-T-MAP year))]
    (-> (* secs 1000)
        int
        ct/millis)))
