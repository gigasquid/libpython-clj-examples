(ns gigasquid.umap
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;;;; you will need all these things below installed
;;; with pip or something else

;;; What is umap? - dimensionality reduction library


(require-python '[seaborn :as sns])
(require-python '[matplotlib.pyplot :as pyplot])
(require-python '[sklearn.datasets :as sk-data])
(require-python '[sklearn.model_selection :as sk-model])
(require-python '[numpy :as numpy])
(require-python '[pandas :as pandas])
(require-python '[umap :as umap])


;;; Code walkthrough from here https://umap-learn.readthedocs.io/en/latest/basic_usage.html


;;; set the defaults for plotting
(sns/set)

;;; IRIS data

;; The next step is to get some data to work with. To ease us into things we’ll start with the iris dataset. It isn’t very representative of what real data would look like, but it is small both in number of points and number of features, and will let us get an idea of what the dimension reduction is doing. We can load the iris dataset from sklearn.

(def iris (sk-data/load_iris))
(print (py.- iris DESCR))

;; Iris plants dataset
;; --------------------

;; **Data Set Characteristics:**

;;     :Number of Instances: 150 (50 in each of three classes)
;;     :Number of Attributes: 4 numeric, predictive attributes and the class
;;     :Attribute Information:
;;         - sepal length in cm
;;         - sepal width in cm
;;         - petal length in cm
;;         - petal width in cm
;;         - class:
;;                 - Iris-Setosa
;;                 - Iris-Versicolour
;;                 - Iris-Virginica
                
;;     :Summary Statistics:

;;     ============== ==== ==== ======= ===== ====================
;;                     Min  Max   Mean    SD   Class Correlation
;;     ============== ==== ==== ======= ===== ====================
;;     sepal length:   4.3  7.9   5.84   0.83    0.7826
;;     sepal width:    2.0  4.4   3.05   0.43   -0.4194
;;     petal length:   1.0  6.9   3.76   1.76    0.9490  (high!)
;;     petal width:    0.1  2.5   1.20   0.76    0.9565  (high!)
;;     ============== ==== ==== ======= ===== ====================

(def iris-df (pandas/DataFrame (py.- iris data) :columns (py.- iris feature_names)))
(py/att-type-map iris-df)

(def iris-name-series (let [iris-name-map (zipmap (range 3) (py.- iris target_names))]
                        (pandas/Series (map (fn [item]
                                              (get iris-name-map item))
                                            (py.- iris target)))))

(py/get-item "species") ;=> nil
(py. iris-df __setitem__ "species" iris-name-series)
(py/get-item iris-df "species")
;; 0         setosa
;; 1         setosa
;; 2         setosa
;; 3         setosa
;; 4         setosa
;;          ...    
;; 145    virginica
;; 146    virginica
;; 147    virginica
;; 148    virginica
;; 149    virginica
;; Name: species, Length: 150, dtype: object


(plot/with-show
  (sns/pairplot iris-df :hue "species"))


;; Time to reduce dimensions
(def reducer (umap/UMAP))

;;; we need to train the reducer to learn about the manifold
;; fit_tranforms first fits the data and then transforms it into a numpy array

(def embedding (py. reducer fit_transform (py.- iris data)))
(py.- embedding shape) ;=>  (150, 2)

;;; 150 samples with 2 column.  Each row of the array is a 2-dimensional representation of the corresponding flower. Thus we can plot the embedding as a standard scatterplot and color by the target array (since it applies to the transformed data which is in the same order as the original).

(first embedding) ;=> [12.449954  -6.0549345]


(let [colors (mapv #(py/get-item (sns/color_palette) %)
                   (py.- iris target))
      x (mapv first embedding)
      y (mapv last embedding)]
 (plot/with-show
   (pyplot/scatter x y :c colors)
   (py. (pyplot/gca) set_aspect "equal" "datalim")
   (pyplot/title "UMAP projection of the Iris dataset" :fontsize 24)))


;;;; Digits Data

(def digits (sk-data/load_digits))
(print (py.- digits DESCR))

;; .. _digits_dataset:

;; Optical recognition of handwritten digits dataset
;; --------------------------------------------------

;; **Data Set Characteristics:**

;;     :Number of Instances: 5620
;;     :Number of Attributes: 64
;;     :Attribute Information: 8x8 image of integer pixels in the range 0..16.
;;     :Missing Attribute Values: None
;;     :Creator: E. Alpaydin (alpaydin '@' boun.edu.tr)
;;     :Date: July; 1998

;;; Plot the images to get an idea of what we are looking at

(plot/with-show
  (let [[fig ax-array] (pyplot/subplots 20 20)
        axes (py. ax-array flatten)]
    (doall (map-indexed (fn [i ax]
                          (py. ax imshow (py/get-item (py.- digits images) i) :cmap "gray_r"))
                        axes))
    (pyplot/setp axes :xticks [] :yticks [] :frame_on false)
    (pyplot/tight_layout :h_pad 0.5 :w_pad 0.01)))

;;; Try to do a scatterplot of the first 10 dimessions for the 64 element long of grayscale values

(def digits-df (pandas/DataFrame (mapv #(take 10 %) (py.- digits data))))
(def digits-target-series (pandas/DataFrame (mapv #(str "Digit " %) (py.- digits target))))
(py. digits-df __setitem__ "digit" digits-target-series)

(plot/with-show
  (sns/pairplot digits-df :hue "digit" :palette "Spectral"))

;;;; use umap with the fit instead

(def reducer (umap/UMAP :random_state 42))
(py. reducer fit (py.- digits data))

;;; now we can look at the embedding attribute on the reducer or call transform on the original data
(def embedding (py. reducer transform (py.- digits data)))
(py.- embedding shape) ;=>(1797, 2)


;; We now have a dataset with 1797 rows (one for each hand-written digit sample), but only 2 columns. As with the Iris example we can now plot the resulting embedding, coloring the data points by the class that theyr belong to (i.e. the digit they represent).

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

;;;; Whooo!

