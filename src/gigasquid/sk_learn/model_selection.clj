(ns gigasquid.sk-learn.model-selection
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;;; from https://scikit-learn.org/stable/tutorial/statistical_inference/model_selection.html

(require-python '[sklearn.datasets :as datasets])
(require-python '[sklearn.model_selection :as model-selection])
(require-python '[sklearn.linear_model :as linear-model])
(require-python '[sklearn.svm :as svm])
(require-python '[numpy :as np])

(def digits (datasets/load_digits :return_X_y true))
(def x-digits (first digits))
(def y-digits (last digits))
(def svc (svm/SVC :C 1 :kernel "linear"))
(def slice-x-digits (->> x-digits (take 100) (into []) (np/array)))
(def slice-y-digits (->> y-digits (take 100) (into []) (np/array)))
(def slice-x2-digits (->> x-digits (drop 100) (take 100) (into []) (np/array)))
(def slice-y2-digits (->> y-digits (drop 100) (take 100) (into []) (np/array)))
(-> svc
    (py. fit slice-x-digits slice-y-digits)
    (py. score slice-x2-digits slice-y2-digits)) ;=> 0.93


;;;; We can split into folds we can use for training and testing
;;; Note here we are doing it a clojure way - but we can use the split method with
;;; indexes later on

(def x-folds (np/array_split x-digits 3))
(def y-folds (np/array_split y-digits 3))

(for [k (range 1 4)]
  (let [[test-x train-x1 train-x2 ](take 3 (drop (dec k) (cycle x-folds)))
        [test-y train-y1 train-y2] (take 3 (drop (dec k) (cycle y-folds)))
        train-x (np/concatenate [train-x1 train-x2])
        train-y (np/concatenate [train-y1 train-y2])]
    (-> svc
        (py. fit train-x train-y)
        (py. score test-x test-y))))
;=>(0.9348914858096828 0.9565943238731218 0.9398998330550918)

;;; Cross Validation generators
;; Scikit-learn has a collection of classes which can be used to generate lists of train/test indices for popular cross-validation strategies.

;; They expose a split method which accepts the input dataset to be split and yields the train/test set indices for each iteration of the chosen cross-validation strategy.

(def X ["a" "a" "a" "b" "b" "c" "c" "c" "c" "c"])
(def k-fold (model-selection/KFold :n_splits 5))
(map (fn [[x y]] {:train x :test y})
     (py. k-fold split X))
;; ({:train [2 3 4 5 6 7 8 9], :test [0 1]}
;;  {:train [0 1 4 5 6 7 8 9], :test [2 3]}
;;  {:train [0 1 2 3 6 7 8 9], :test [4 5]}
;;  {:train [0 1 2 3 4 5 8 9], :test [6 7]}
;;  {:train [0 1 2 3 4 5 6 7], :test [8 9]})


;;; let's understand the generateor for the split and how to use indexes on numpy
(def try-x (first (py. k-fold split x-digits)))
(def indexes (first try-x))
(py.- x-digits  shape) ;->   (1797, 64)
(py.- indexes shape) ;=>   (1437,)
;;;; You can use py/get-item to get indexes from numpy
(def test-items (py/get-item x-digits indexes))
(py.- test-items shape) ;=>  (1437, 64)



(map (fn [[train-indexes test-indexes]]
       (-> svc
           (py. fit (py/get-item x-digits train-indexes)
                    (py/get-item y-digits train-indexes))
           (py. score (py/get-item x-digits test-indexes)
                (py/get-item y-digits test-indexes))))
     (py. k-fold split x-digits))
;=>(0.9638888888888889 0.9222222222222223 0.9637883008356546 0.9637883008356546 0.9303621169916435)

;; The cross-validation score can be directly calculated using the cross_val_score helper. Given an estimator, the cross-validation object and the input dataset, the cross_val_score splits the data repeatedly into a training and a testing set, trains the estimator using the training set and computes the scores based on the testing set for each iteration of cross-validation

;;; n_jobs=-1 means the computation will use all cpus
(model-selection/cross_val_score svc x-digits y-digits :cv k-fold :n_jobs -1)
                                        ;=>[0.96388889 0.92222222 0.9637883  0.9637883  0.93036212]

;Alternatively, the scoring argument can be provided to specify an alternative scoring method.
(model-selection/cross_val_score svc x-digits y-digits :cv k-fold
                                 :scoring "precision_macro")
;=>[0.96578289 0.92708922 0.96681476 0.96362897 0.93192644]


;;;; Grid search
;;scikit-learn provides an object that, given data, computes the score during the fit of an estimator on a parameter grid and chooses the parameters to maximize the cross-validation score. This object takes an estimator during the construction and exposes an estimator API:

(def Cs (np/logspace -6 -1 10))
(def clf (model-selection/GridSearchCV :estimator svc
                                       :param_grid {:C Cs}
                                       :n_jobs -1))
(def slice-x-digits (->> x-digits (take 1000) (into []) (np/array)))
(def slice-y-digits (->> y-digits (take 1000) (into []) (np/array)))
(def slice-x2-digits (->> x-digits (drop 1000) (take 1000) (into []) (np/array)))
(def slice-y2-digits (->> y-digits (drop 1000) (take 1000) (into []) (np/array)))
(py. clf fit slice-x-digits slice-y-digits)
(py.- clf best_score_) ;=> 0.95
(-> clf (py.- best_estimator_) (py.- C)) ;=> 0.0021544346900318843
(py. clf score slice-x2-digits slice-y2-digits) ;=> 0.946047678795483


;;; Nested cross validation
(model-selection/cross_val_score clf x-digits y-digits)
;;=>[0.94722222 0.91666667 0.96657382 0.97493036 0.93593315]


;; Cross-validated esitmators

(def lasso (linear-model/LassoCV))
(def diabetes (datasets/load_diabetes :return_X_y true))
(def x-diabetes (first diabetes))
(def y-diabetes (last diabetes))
(py. lasso fit x-diabetes y-diabetes)
;;; The estimator chose automatically its lambda:
(py.- lasso alpha_);=> 0.003753767152692203

