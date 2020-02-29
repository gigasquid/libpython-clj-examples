(ns gigasquid.-configure
  (:require
   [libpython-clj.python :as py]))

; local install
; (py/initialize! :python-executable "/usr/bin/python3.8"
;                 :library-path "/usr/lib/libpython3.8.so.1.0")

; virtualenv @ "env" directory
; (py/initialize! :python-executable "env/bin/python3.8"
;                 :library-path "/usr/lib/libpython3.so")
