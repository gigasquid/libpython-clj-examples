(ns gigasquid.sk-learn.pipelining
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;We have seen that some estimators can transform data and that some estimators can predict variables. We can also create combined estimators:

(require-python '[sklearn.datasets :as datasets])
(require-python '[sklearn.decomposition :as decomposition])
(require-python '[sklearn.linear_model :as linear-model])
(require-python '[sklearn.pipeline :as pipeline])
(require-python '[sklearn.model_selection :as model-selection])
(require-python '[numpy :as np])
(require-python '[matplotlib.pyplot :as pyplot])

;; Define a pipeline to search for the best combination of PCA truncation
;; and classifier regularization.
(def pca (decomposition/PCA))
(def logistic (linear-model/LogisticRegression :max_iter 10000 :tol 0.1))
(def pipe (pipeline/Pipeline :steps [ ["pca" pca] ["logistic" logistic]]))

(def digits (datasets/load_digits :return_X_y true))
(def X-digits (first digits))
(def y-digits (last digits))

;;; Parameters of pipelines can be set using ‘__’ separated parameter names:

(def logspace (np/logspace -4 4 4))
(def param-grid {"pca__n_components" [5 15 30 45 64]
                 "logistic__C" logspace})


(def search (model-selection/GridSearchCV :estimator pipe
                                          :param_grid param-grid
                                          :n_jobs -1))
(py. search fit X-digits y-digits)
(py.- search best_score_);=> 0.9198885793871865
(py.- search best_params_)
                                        ;=>{'logistic__C': 0.046415888336127774, 'pca__n_components': 45}

;;; Plot the PCA Spectrum
(py. pca fit X-digits)

(plot/with-show
  (let [[fig axes] (pyplot/subplots :nrows 2 :sharex true :figsize [6 6])
        val1 (np/arange 1 (inc (py.- pca n_components_)))
        val2 (py.- pca explained_variance_ratio_)
        ax0 (first axes)
        ax1 (last axes)
        val3 (-> (py.- search best_estimator_)
                 (py.- named_steps)
                 (py/get-item "pca")
                 (py.- n_components))]
    (py. ax0 plot val1 val2 "+" :linewidth 2)
    (py. ax0 set_ylabel "PCA explained variance ratio")
    (py. ax0 axvline val3 :linestyle ":" :label "n_components chosen")
    (py. ax0 legend :prop {"size" 12}))
  )

