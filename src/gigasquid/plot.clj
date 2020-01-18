(ns gigasquid.plot
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [clojure.java.shell :as sh]))


;;; This uses the headless version of matplotlib to generate a graph then copy it to the JVM
;; where we can then print it

;;;; have to set the headless mode before requiring pyplot
(def mplt (py/import-module "matplotlib"))
(py. mplt "use" "Agg")

(require-python 'matplotlib.pyplot)
(require-python 'matplotlib.backends.backend_agg)
(require-python 'numpy)


(defmacro with-show
  "Takes forms with mathplotlib.pyplot to then show locally"
  [& body]
  `(let [_# (matplotlib.pyplot/clf)
         fig# (matplotlib.pyplot/figure)
         agg-canvas# (matplotlib.backends.backend_agg/FigureCanvasAgg fig#)]
     ~(cons 'do body)
     (py. agg-canvas# "draw")
     (matplotlib.pyplot/savefig "temp.png")
     (sh/sh "open" "temp.png")))

(comment
  (def x (numpy/linspace 0 2 100))

  (with-show
    (matplotlib.pyplot/plot [x x] :label "linear")
    (matplotlib.pyplot/plot [x (py. x "__pow__" 2)] :label "quadratic")
    (matplotlib.pyplot/plot [x (py. x "__pow__" 3)] :label "cubic")
    (matplotlib.pyplot/xlabel "x label")
    (matplotlib.pyplot/ylabel "y label")
    (matplotlib.pyplot/title "Simple Plot"))


  (with-show (matplotlib.pyplot/plot [[1 2 3 4 5] [1 2 3 4 10]] :label "linear"))
  )






