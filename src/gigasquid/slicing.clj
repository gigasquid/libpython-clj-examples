(ns gigasquid.slicing
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]))


(require-python '[builtins :as python])

;;https://data-flair.training/blogs/python-slice/

(def l (py/->py-list [1 2 3 4]))

;;; slice object slice(stop) or slice(start, stop, step)

;;; sub elements 2 3 4


(python/slice 3) ;=> slice(None, 3, None)
(py/get-item l (python/slice 3)) ;=> [1, 2, 3]


;;; with specifiying interval
(py/get-item l (python/slice 1 3)) ;=> [2, 3]

;;; negative indices go from right to left
(py/get-item l (python/slice -3 -1)) ;=> [2, 3]


;;; python slicing tuples

(def t (py/->py-list [1 2 3 4 5]))
(py/get-item t (python/slice 2 4)) ;=> [3, 4]

(py/get-item t (python/slice -1 -5 -2)) ;=> [5, 3]

;;; is equivalent to t[-1:-5:-2]


;;; t[:3] From 0 to 2
;;; is the same as
(py/get-item t (python/slice nil 3)) ;=> [1, 2, 3]


;;; t[3:] From 3 to the end
;; is the same as
(py/get-item t (python/slice 3 nil)) ;=> [4, 5]

;;; t[:] From beginning to the end
;;; is the same as
(py/get-item t (python/slice nil nil)) ;=>[1, 2, 3, 4, 5]


;;;; Extended Python slices with step value

;;; t[::-1] reverse
(py/get-item t (python/slice nil nil -1)) ;=>  [5, 4, 3, 2, 1]


;;; t[::-2] Reverse with step=2
(py/get-item t (python/slice nil nil -2)) ;=>  [5, 3, 1]


;; t[:5:-1] Index 5 to end (already ahead of that), right to left; results in empty tuple
(py/get-item t (python/slice nil 5 -1)) ;=> []
