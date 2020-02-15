(ns gigasquid.sk-learn.unsupervised-learning
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;; from https://scikit-learn.org/stable/tutorial/statistical_inference/unsupervised_learning.html

(require-python '[sklearn.datasets :as datasets])
(require-python '[sklearn.cluster :as cluster])
(require-python '[sklearn.feature_extraction :as feature-extraction])
(require-python '[sklearn.decomposition :as decomposition])
(require-python '[numpy :as np])
(require-python '[numpy.random :as np-random])
(require-python '[scipy.signal :as signal])

;;; K-means clustering
(def iris (datasets/load_iris :return_X_y true))
(def iris-x (first iris))
(def iris-y (last iris))

(def k-means (cluster/KMeans :n_clusters 3))
(py. k-means fit iris-x)
(take-last 10  (py.- k-means labels_));=> (2 2 0 2 2 2 0 2 2 0)
(take-last 10 iris-y) ;=> (2 2 2 2 2 2 2 2 2 2)

;;; Feature agglomeration
;; We have seen that sparsity could be used to mitigate the curse of dimensionality, i.e an insufficient amount of observations compared to the number of features. Another approach is to merge together similar features: feature agglomeration. This approach can be implemented by clustering in the feature direction, in other words clustering the transposed data.
(def digits (datasets/load_digits))
(def images (py.- digits images))
(def X (np/reshape images [(py/len images) -1]))
(py.- (first images) shape) ;=>  (8, 8)
(def connectivity (feature-extraction/grid_to_graph 8 8))
(def agglo (cluster/FeatureAgglomeration :connectivity connectivity :n_clusters 32))
(py. agglo fit X)
(def X-reduced (py. agglo transform X))
(def X-approx (py. agglo inverse_transform X-reduced))
(def images-shape (py.- images shape))
images-shape ;=> (1797, 8, 8)
(def images-approx (np/reshape X-approx images-shape))

;;; Principal component analyis : PCA

;; Create a signal with only 2 useful dimensions
(def x1 (np-random/normal :size 100))
(def x2 (np-random/normal :size 100))
(def x3 (np/add x1 x2))
(def X (np/column_stack [x1 x2 x3]))
(def pca (decomposition/PCA))
(py. pca fit X)
(py.- pca explained_variance_) ;=> [2.90691814e+00 9.90171666e-01 2.83277241e-31]

;; As we can see, only the 2 first components are useful
(py/att-type-map pca)
(py/set-attr! pca "n_components" 2)
(py.- pca n_components) ;=>2
(def X-reduced (py. pca fit_transform X))
(py.- X-reduced shape);=>  (100, 2)

;;;Independent Component Analysis: ICA
;;Independent component analysis (ICA) selects components so that the distribution of their loadings carries a maximum amount of independent information. It is able to recover non-Gaussian independent signals:

;; Generate the sample data
(def time (np/linspace 0 10 2000))
(def s1 (np/sin (np/multiply 2 time)))
(def s2 (np/sign (np/sin (np/multiply 3 time))))
(def s3 (signal/sawtooth (np/multiply 2 np/pi time)))
(def S (np/column_stack [s1 s2 s3]))
(def S (np/add S 0.2))
