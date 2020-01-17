(ns gigasquid.plot
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [tech.libs.buffered-image :as bufimg]
            [tech.v2.datatype :as dtype]
            [tech.v2.tensor :as dtt]
            [clojure.java.shell :as sh]))


;;; This uses the headless version of matplotlib to generate a graph then copy it to the JVM
;; where we can then print it

;;;; have to set the headless mode before requiring pyplot
(def mplt (py/import-module "matplotlib"))
(py. mplt "use" "Agg")

(require-python 'matplotlib.pyplot)
(require-python 'matplotlib.backends.backend_agg)
(require-python 'numpy)


(def fig (matplotlib.pyplot/figure))
(def agg-canvas (matplotlib.backends.backend_agg/FigureCanvasAgg fig))



(defn plot []
  (do
    (matplotlib.pyplot/plot [[1 2 3 4 5] [1 2 3 4 10]] "go")
   (let [_ (py. agg-canvas "draw")
         np-data (numpy/array (py. agg-canvas "buffer_rgba"))
         tens (py/as-tensor np-data)
         bufimage (bufimg/new-image 480 640 :byte-abgr)]
     (dtt/select tens :all :all (->> (range 4) reverse))
     (dtype/copy! tens bufimage)
     (bufimg/save! bufimage "temp.png")
     (sh/sh "open" "temp.png"))))


(defn pyplot
  "Returns the configured pyplot module to use from other namespaces"
  [])


(defmacro with-show
  "Takes forms with mathplotlib.pyplot to then show locally"
  [& body]
  `(let [fig# (matplotlib.pyplot/figure)
        agg-canvas# (matplotlib.backends.backend_agg/FigureCanvasAgg fig#)
        ignored#  ~(cons 'do body)
        ignored# (py. agg-canvas# "draw")
        np-data# (numpy/array (py. agg-canvas# "buffer_rgba"))
        tens# (py/as-tensor np-data#)
        bufimage# (bufimg/new-image 480 640 :byte-abgr)]
    (dtype/copy! tens# bufimage#)
    (dtt/select tens# :all :all (->> (range 4) reverse))
    (bufimg/save! bufimage# "temp.png")
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

(comment

  (let [fig (matplotlib.pyplot/figure)
        agg-canvas (matplotlib.backends.backend_agg/FigureCanvasAgg fig)
        ignored  (matplotlib.pyplot/plot [[1 2 3 4 5] [1 2 3 4 10]] :label "linear")
        ignored (py. agg-canvas "draw")
        np-data (numpy/array (py. agg-canvas "buffer_rgba"))
        tens (py/as-tensor np-data)
        bufimage (bufimg/new-image 480 640 :byte-abgr)]
    (dtype/copy! tens bufimage)
    (dtt/select tens :all :all (->> (range 4) reverse))
    (bufimg/save! bufimage "temp.png")
    (sh/sh "open" "temp.png"))
  )
;;; now the rendering






