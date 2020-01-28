(ns gigasquid.seaborn
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

(require-python '[seaborn :as sns])
(require-python '[matplotlib.pyplot :as pyplot])

;;; What is seaborn? Really cool statistical plotting

;;; sudo pip3 install seaborn

(sns/set) ;;; set default style

;;; code tutorial from https://seaborn.pydata.org/introduction.html

(def dots (sns/load_dataset "dots"))
(py. dots head)
;;   align  ... firing_rate
;; 0  dots  ...   33.189967
;; 1  dots  ...   31.691726
;; 2  dots  ...   34.279840
;; 3  dots  ...   32.631874
;; 4  dots  ...   35.060487

;; [5 rows x 5 columns]

(take 5 dots) ;=> ("align" "choice" "time" "coherence" "firing_rate")
;; seaborn will be most powerful when your datasets have a particular organization. This format is alternately called “long-form” or “tidy” data and is described in detail by Hadley Wickham in this academic paper. The rules can be simply stated:

;; Each variable is a column

;; Each observation is a row

;;;; statistical relationship plotting

(plot/with-show
  (sns/relplot :x "time" :y "firing_rate" :col "align"
               :hue "choice" :size "coherence" :style "choice"
               :facet_kws {:sharex false} :kind "line"
               :legend "full" :data dots))

;;;; statistical estimateion and error bars

(def fmri (sns/load_dataset "fmri"))

(plot/with-show
  (sns/relplot :x "timepoint" :y "signal" :col "region"
               :hue "event" :style "event" :kind "line"
               :data fmri))

;;; enhance a scatter plot to include a linear regression model

(def tips (sns/load_dataset "tips"))
(plot/with-show
  (sns/lmplot :x "total_bill" :y "tip" :col "time" :hue "smoker" :data tips))

;;; data analysis between caterogical values

(plot/with-show
  (sns/catplot :x "day" :y "total_bill" :hue "smoker" :kind "swarm" :data tips))

(plot/with-show
  (sns/catplot :x "day" :y "total_bill" :hue "smoker" :kind "bar" :data tips))

;;; visualizing dataset structure

(def iris (sns/load_dataset "iris"))
(plot/with-show
  (sns/jointplot :x "sepal_length" :y "petal_length" :data iris))

(plot/with-show
  (sns/pairplot :data iris :hue "species"))
