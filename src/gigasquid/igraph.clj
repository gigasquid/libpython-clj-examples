(ns gigasquid.igraph
 (:require [libpython-clj.require :refer [require-python]]
           [libpython-clj.python :as py :refer [py. py.. py.-]]))

;;; https://igraph.org/python/doc/tutorial/tutorial.html#creating-a-graph-from-scratch

;;; igraph is a graph python library

;;; sudo pip3 install python-igraph
;;; sudo pip3 install pycairo


(require-python '[igraph :as igraph])

(def g (igraph/Graph))
(py. g add_vertices 3)
(py. g add_edges [[0 1] [1 2]])

;;; it's very stateful from here but
(doto g
  (py. add_edges [[2 0]])
  (py. add_vertices 3)
  (py. add_edges [[2 3] [3 4] [4 5] [5 3]]))

(igraph/summary g)
;;; IGRAPH U--- 6 7 --


(def g2 (py. (igraph/Graph) Famous "petersen"))
;;; this actually works fine one I installed everything
;;; the image will show up
(def plot (igraph/plot g2))
;;; save the image to disk
(py. plot save "myplot.png")
