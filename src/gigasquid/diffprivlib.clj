(ns gigasquid.diffprivlib
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;;; From https://github.com/IBM/differential-privacy-library

;;; Install: pip install diffprivlib

(require-python '[sklearn.datasets :as datasets])
(require-python '[sklearn.model_selection :as model-selection])
(require-python '[matplotlib.pyplot :as pyplot])
(require-python '[numpy :as np])
(require-python '[diffprivlib.models :as models])
(require-python '[sklearn.metrics :as metrics])
(require-python '[builtins :as python])

;;; Using the iris dataset - load with 80/20 split

(def dataset (datasets/load_iris))
(def iris-data (let [[X-train X-test y-train y-test]
                      (model-selection/train_test_split (py.- dataset data)
                                                        (py.- dataset target)
                                                        :test_size 0.2)]
                 {:X-train X-train :X-test X-test
                  :y-train y-train :y-test y-test}))

;; Now, let's train a differentially private naive Bayes classifier. Our classifier runs just like an sklearn classifier, so you can get up and running quickly.

;; diffprivlib.models.GaussianNB can be run without any parameters, although this will throw a warning (we need to specify the bounds parameter to avoid this). The privacy level is controlled by the parameter epsilon, which is passed to the classifier at initialisation (e.g. GaussianNB(epsilon=0.1)). The default is epsilon = 1.0.

(def clf (models/GaussianNB))
(py. clf fit (:X-train iris-data) (:y-train iris-data))

;; We can now classify unseen examples, knowing that the trained model is differentially private and preserves the privacy of the 'individuals' in the training set (flowers are entitled to their privacy too!).

(py. clf predict (:X-test iris-data))

;;=> [1 0 1 1 1 2 1 0 2 2 2 2 1 0 0 2 1 0 1 0 0 1 0 1 2 2 0 2 1 1]

;;We can easily evaluate the accuracy of the model for various epsilon values and plot it with matplotlib.

(def epsilons (np/logspace -2 2 50))
(def bounds (python/list [(python/tuple [4.3 7.9]) (python/tuple [2.0 4.4])
                          (python/tuple [1.1 6.9]) (python/tuple [0.1 2.5])]))

(def accuracy (mapv (fn [epsilon]
                      (let [clf (models/GaussianNB :bounds bounds :epsilon epsilon)
                            _ (py. clf fit (:X-train iris-data) (:y-train iris-data))
                            predictions (->> (:X-test iris-data)
                                             (py. clf predict))]
                        (metrics/accuracy_score(:y-test iris-data) predictions)))
                    epsilons))

accuracy
;;=> [0.3333333333333333 0.36666666666666664 0.36666666666666664 0.36666666666666664 0.36666666666666664 0.2 0.3333333333333333 0.3 0.3333333333333333 0.3333333333333333 0.3 0.3 0.6 0.5666666666666667 0.2 0.7 0.6 0.1 0.6666666666666666 0.9 0.6666666666666666 0.6666666666666666 1.0 0.6 0.8 0.7666666666666667 0.8666666666666667 0.8333333333333334 0.9333333333333333 0.8666666666666667 0.9 1.0 0.9333333333333333 0.9333333333333333 0.9 0.9333333333333333 0.8333333333333334 1.0 0.8 0.8 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0]

(plot/with-show-one
  (pyplot/semilogx epsilons accuracy)
  (pyplot/title "Differentially private Naive Bayes accuracy")
  (pyplot/xlabel "epsilon")
  (pyplot/ylabel "Accuracy"))
