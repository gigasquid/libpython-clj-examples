(ns gigasquid.trimap
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;;;; you will need all the below libraries pip installed

;;; What is Trimap? It is a dimensionality reduction library (like umap) but using a different algorithim
;;https://pypi.org/project/trimap/

;;; also see the umap.clj example

(require-python '[trimap :as trimap])
(require-python '[sklearn.datasets :as sk-data])
(require-python '[matplotlib.pyplot :as pyplot])

(def digits (sk-data/load_digits))
(def digits-data (py.- digits data))

(def embedding (py. (trimap/TRIMAP) fit_transform digits-data))
(py.- embedding shape) ;=> (1797, 2)


;; We now have a dataset with 1797 rows (one for each hand-written digit sample), but only 2 columns. We can now plot the resulting embedding, coloring the data points by the class that theyr belong to (i.e. the digit they represent).

(plot/with-show
  (let [x (mapv first embedding)
        y (mapv last embedding)
        colors (py.- digits target)
        bounds (numpy/subtract (numpy/arange 11) 0.5)
        ticks (numpy/arange 10)]
    (pyplot/scatter x y :c colors :cmap "Spectral" :s 5)
    (py. (pyplot/gca) set_aspect "equal" "datalim")
    (py. (pyplot/colorbar :boundaries bounds) set_ticks ticks)
    (pyplot/title "UMAP projection of the Digits dataset" :fontsize 24)))
