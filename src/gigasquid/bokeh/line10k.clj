(ns gigasquid.bokeh.line10k
  (:require [libpython-clj.require
             :refer [require-python]]
            [libpython-clj.python
             :as py
             :refer [py.
                     py..
                     py.-
                     att-type-map
                     ->python
                     ->jvm
                     ]]
            [clojure.java.shell :as sh]
            [clojure.pprint :refer [pprint]])
  (:import [java.io File]))

;;; Python installation
;;; pip3 install bokeh
;;; pip3 install numpy

;; Based on: https://github.com/bokeh/bokeh/blob/1.4.0/examples/webgl/line10k.py

;; First require the basic package
(py/from-import bokeh.plotting figure output_file show curdoc)
(require-python '[bokeh.plotting :as bkp])
(require-python '[numpy :as np])
(require-python '[numpy.random :as np-random])
(require-python '[builtins :as python])

;; Note:
#_(= np/pi Math/PI) ;;=> true

(let [N 10000
      x (np/linspace 0 (* 10 np/pi) N)
      y (np/add
         (np/cos x)
         (np/sin (np/add (np/multiply 2 x) 1.25))
         (np-random/normal 0 0.001 (python/tuple [N])))
      p (bkp/figure :title "A line consisting of 10k points"
                    :output_backend "webgl")]

  (py. p line x y :color "#22aa22" :line_width 3)
  ;; Tips:
  ;; To save the output to a file you can use the next line
  #_(output_file "line10.html" :title "line10k example")

  ;; Or simply show it immediately via your browser
  (bkp/show p))
