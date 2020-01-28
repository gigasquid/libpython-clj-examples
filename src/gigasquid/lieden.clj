(ns gigasquid.lieden
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [clojure.java.shell :as sh]))

;;;sudo pip3 install leidenalg

;;; you also need to make sure igraph is working and installed too (see igraph.clj)

;;; What is leidenalg? https://github.com/vtraag/leidenalg
;; Implementation of the Leiden algorithm for various quality functions to be used with igraph in Python.
;;; sudo pip3 install pycairo

(require-python '[igraph :as ig])
(require-python '[leidenalg :as la])

;;https://leidenalg.readthedocs.io/en/latest/intro.html

;;Let us then look at one of the most famous examples of network science: the Zachary karate club (it even has a prize named after it):
(def G (py. (ig/Graph) Famous "Zachary"))

;;;Now detecting communities with modularity is straightforward


(def partition (la/find_partition G la/ModularityVertexPartition))

;;; plotting results

(def plot (ig/plot partition))

;;; save the plot png

(py. plot save "zach.png")


