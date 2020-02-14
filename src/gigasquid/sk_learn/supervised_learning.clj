(ns gigasquid.sk-learn.supervised-learning
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))


;;; From https://scikit-learn.org/stable/tutorial/statistical_inference/supervised_learning.html

;; Clasifying irises

(require-python '[numpy :as np])
(require-python '[numpy.random :as np-random])
(require-python '[sklearn.datasets :as datasets])
(require-python '[matplotlib.pyplot :as pyplot])

(def iris (datasets/load_iris :return_X_y true))
(def iris-x (first iris))
(def iris-y (last iris))
(take 2 iris-x) ;=> ([5.1 3.5 1.4 0.2] [4.9 3.  1.4 0.2])
(take 2 iris-y) ;=> (0 0)
(np/unique iris-y) ;=>  [0 1 2]


;;; K-Nearest neighbors classifier

;;; The simplest possible classifier is the nearest neighbor: given a new observation X_test, find in the training set (i.e. the data used to train the estimator) the observation with the closest feature vector.

;;Split iris data in train and test data
;; A random permutation, to split the data randomly

;;; here instead of following the python example we are going to use
;; shuffle and take instead

(py.- iris-x shape) ;=> (150, 4)
(py.- iris-y shape) ;=> (150,)
(def shuffled-data (->> (map (fn [x y] {:x x :y y}) iris-x iris-y)
                        (shuffle)))
(def train-data (take 140 shuffled-data))
(def test-data (drop 140 shuffled-data))
(count train-data) ;->  140
(count test-data) ;-> 10
(def iris-x-train (mapv :x train-data))
(def iris-y-train (mapv :y train-data))
(def iris-x-test (mapv :x test-data))
(def iris-y-test (mapv :y test-data))


(require-python '[sklearn.neighbors :as neighbors])
(def knn (neighbors/KNeighborsClassifier))
(py. knn fit iris-x-train iris-y-train)
;;; predict
(py. knn predict iris-x-test) ;=>  [0 0 1 2 2 0 2 2 0 2]
;;; actual test
iris-y-test  ;=> [0 0 1 2 2 0 2 1 0 2]


;;; Linear model - From regression to sparsity
;; Diabetes dataset

;;The diabetes dataset consists of 10 physiological variables (age, sex, weight, blood pressure) measure on 442 patients, and an indication of disease progression after one year:

(require-python '[sklearn.linear_model :as linear-model])

(def diabetes (datasets/load_diabetes :return_X_y true))
(def diabetes-x (first diabetes))
(def diabetes-y (last diabetes))
(py.- diabetes-x shape);=>  (442, 10)
(- 442 20) ;=> 422
(def diabetes-x-train (->> diabetes-x (take 422) (into []) (np/array)))
(def diabetes-y-train (->> diabetes-y (take 422) (into []) (np/array)))
(def diabetes-x-test (->> diabetes-x (drop 422) (into []) (np/array)))
(def diabetes-y-test (->> diabetes-y (drop 422) (into []) (np/array)))


;;LinearRegression, in its simplest form, fits a linear model to the data set by adjusting a set of parameters in order to make the sum of the squared residuals of the model as small as possible.

(py/python-type diabetes-x-train);=>  :ndarray
(py.- diabetes-x shape);=>  (442, 10)
(py.- diabetes-x-train shape);=>  (422, 10)

(def regr (linear-model/LinearRegression))
(py. regr fit diabetes-x-train diabetes-y-train)
(py.- regr coef_)

;; [ 3.03499549e-01 -2.37639315e+02  5.10530605e+02  3.27736980e+02
;;  -8.14131709e+02  4.92814588e+02  1.02848452e+02  1.84606489e+02
;;  7.43519617e+02  7.60951722e+01]

;;; The mean square error
(np/mean
 (np/square
  (np/subtract (py. regr predict diabetes-x-test) diabetes-y-test)));=> 13.41173112391975
(py. regr score diabetes-x diabetes-y);=> 0.5175336599402476

;;; shrinkage
;;If there are few data points per dimension, noise in the observations induces high variance:
(def X [[0.5] [1]])
(def Y [0.5 1])
(def test [[0] [2]])
(def regr (linear-model/LinearRegression))

(np-random/seed 0)
(plot/with-show
  (dotimes [i 6]
    (let [this-x (np/multiply 0.1
                              (np/add
                               (np-random/normal :size [2 1]) X))
          _  (py. regr fit this-x Y)
          prediction (py. regr predict test)]
      (pyplot/plot test prediction)
      (pyplot/scatter this-x Y :s 3))))

;;A solution in high-dimensional statistical learning is to shrink the regression coefficients to zero: any two randomly chosen set of observations are likely to be uncorrelated. This is called Ridge regression:

(def regr (linear-model/Ridge :alpha 1))
(plot/with-show
  (dotimes [i 6]
    (let [this-x (np/multiply 0.1
                              (np/add
                               (np-random/normal :size [2 1]) X))
          _  (py. regr fit this-x Y)
          prediction (py. regr predict test)]
      (pyplot/plot test prediction)
      (pyplot/scatter this-x Y :s 3))))

;; This is an example of bias/variance tradeoff: the larger the ridge alpha parameter, the higher the bias and the lower the variance.

;; We can choose alpha to minimize left out error, this time using the diabetes dataset rather than our synthetic data:

(def alphas (np/logspace -4 -1 6))
(mapv #(-> regr
        (py. set_params :alpha %)
        (py. fit diabetes-x-train diabetes-y-train)
        (py. score diabetes-x-test diabetes-y-test))
      alphas)
;-=>[0.5851110683883531 0.5852073015444674 0.585467754069849 0.5855512036503915 0.5830717085554161 0.570589994372801]


;;; Sparsity  
(def regr (linear-model/Lasso))
(def scores (map #(-> regr
                      (py. set_params :alpha %)
                      (py. fit diabetes-x-train diabetes-y-train)
                      (py. score diabetes-x-test diabetes-y-test))
                 alphas))
(def best-alpha (->> (map (fn [a s] {:alpha a :score s}) alphas scores)
                     (sort-by :score)
                     last))
(-> regr
    (py. set_params :alpha best-alpha)
    (py. fit diabetes-x-train diabetes-y-train)
    (py.- coef_))

;; [   0.         -212.43764548  517.19478111  313.77959962 -160.8303982
;;    -0.         -187.19554705   69.38229038  508.66011217   71.84239008]


;;;; Classification
;; For classification, as in the labeling iris task, linear regression is not the right approach as it will give too much weight to data far from the decision frontier. A linear approach is to fit a sigmoid function or logistic function:

(def log (linear-model/LogisticRegression :C 1e5))
;;The C parameter controls the amount of regularization in the LogisticRegression object: a large value for C results in less regularization. penalty="l2" gives Shrinkage (i.e. non-sparse coefficients), while penalty="l1" gives Sparsity.
(py. log fit iris-x-train iris-y-train)
(py. log score iris-x-test iris-y-test);=> 1.0


;;;; Support Vector Machines

(require-python '[sklearn.svm :as svm])

(def svc (svm/SVC :kernel "linear"))
(py. svc fit iris-x-train iris-y-train)
(;; C=1.0, break_ties=False, cache_size=200, class_weight=None, coef0=0.0,
 ;;        decision_function_shape='ovr', degree=3, gamma='scale', kernel='linear',
 ;;        max_iter=-1, probability=False, random_state=None, shrinking=True,
 ;;        tol=0.001, verbose=False)

