(ns gigasquid.sk-learn.datasets-estimators
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

(require-python '[sklearn.datasets :as datasets])
(require-python '[matplotlib.pyplot :as pyplot])
(require-python '[matplotlib.pyplot.cm :as pyplot-cm])

;;;; From https://scikit-learn.org/stable/tutorial/statistical_inference/settings.html

;;; Taking a look as the standard iris dataset

(def iris (datasets/load_iris))
(def data (py.- iris data))
(py.- data shape);->  (150, 4)

;;; It is made of 150 observations of irises, each described by 4 features: their sepal and petal length and width

;;; An example of reshaping is with the digits dataset
;;; The digits dataset is made of 1797 8x8 images of hand-written digits

(def digits (datasets/load_digits))
(def digit-images (py.- digits images))
(py.- digit-images shape) ;=>  (1797, 8, 8)

(plot/with-show
  (pyplot/imshow (last digit-images) :cmap pyplot-cm/gray_r))

;;; To use this dataset we transform each 8x8 image to feature vector of length 64

(def data  (py. digit-images reshape (first (py.- digit-images shape)) -1))

(py.- data shape) ;=> (1797, 64)


;;;; Estimator objects

;;An estimator is any object that learns from data
                                        ; it may be a classification, regression or clustering algorithm or a transformer that extracts/filters useful features from raw data.

;;All estimator objects expose a fit method that takes a dataset (usually a 2-d array

