(ns gigasquid.facebook-prophet
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;;; sudo pip3 install  holidays==0.9.12
;;; sudo pip3 install plotly (optional)

;;; tutorial https://facebook.github.io/prophet/docs/quick_start.html#python-api

(require-python '[pandas :as pd])
(require-python '[fbprophet :as fbprophet])
(require-python '[matplotlib.pyplot :as pyplot])

;; The input to Prophet is always a dataframe with two columns: ds and y. The ds (datestamp) column should be of a format expected by Pandas, ideally YYYY-MM-DD for a date or YYYY-MM-DD HH:MM:SS for a timestamp. The y column must be numeric, and represents the measurement we wish to forecast.

;; As an example, let’s look at a time series of the log daily page views for the Wikipedia page for Peyton Manning. We scraped this data using the Wikipediatrend package in R. Peyton Manning provides a nice example because it illustrates some of Prophet’s features, like multiple seasonality, changing growth rates, and the ability to model special days (such as Manning’s playoff and superbowl appearances). The CSV is available here.

(def csv-file (slurp "https://raw.githubusercontent.com/facebook/prophet/master/examples/example_wp_log_peyton_manning.csv"))
(spit "manning.csv" csv-file)
(def df (pd/read_csv "manning.csv"))
(py.- df head)
;; <bound method NDFrame.head of               ds          y
;; 0     2007-12-10   9.590761
;; 1     2007-12-11   8.519590
;; 2     2007-12-12   8.183677
;; 3     2007-12-13   8.072467
;; 4     2007-12-14   7.893572
;; ...          ...        ...
;; 2900  2016-01-16   7.817223
;; 2901  2016-01-17   9.273878
;; 2902  2016-01-18  10.333775
;; 2903  2016-01-19   9.125871
;; 2904  2016-01-20   8.891374

;; [2905 rows x 2 columns]>

;; We fit the model by instantiating a new Prophet object. Any settings to the forecasting procedure are passed into the constructor. Then you call its fit method and pass in the historical dataframe. Fitting should take 1-5 seconds.

(def m (fbprophet/Prophet))
(py. m fit df)

;; Predictions are then made on a dataframe with a column ds containing the dates for which a prediction is to be made. You can get a suitable dataframe that extends into the future a specified number of days using the helper method Prophet.make_future_dataframe. By default it will also include the dates from the history, so we will see the model fit as well.

(def future (py. m make_future_dataframe :periods 365))
(py.- future tail)
;; <bound method NDFrame.tail of              ds
;; 0    2007-12-10
;; 1    2007-12-11
;; 2    2007-12-12
;; 3    2007-12-13
;; 4    2007-12-14
;; ...         ...
;; 3265 2017-01-15
;; 3266 2017-01-16
;; 3267 2017-01-17
;; 3268 2017-01-18
;; 3269 2017-01-19

;; [3270 rows x 1 columns]>

;; The predict method will assign each row in future a predicted value which it names yhat. If you pass in historical dates, it will provide an in-sample fit. The forecast object here is a new dataframe that includes a column yhat with the forecast, as well as columns for components and uncertainty intervals.

(def forecast (py. m predict future))
(py.- forecast yhat_lower)
(py/att-type-map forecast)
(def vals (py. forecast __array__ ["ds" "yhat" "yhat_lower" "yhat_upper"]))
(py/python-type vals) ;=>  :ndarray
;; [[Timestamp('2007-12-10 00:00:00') 8.041238819642132 8.219483670063799
;;   ... 0.0 0.0 8.844169826770502]
;;  [Timestamp('2007-12-11 00:00:00') 8.039694770587365 8.037913913183381
;;   ... 0.0 0.0 8.592697395711903]
;;  [Timestamp('2007-12-12 00:00:00') 8.038150721532599 7.768551313613439
;;   ... 0.0 0.0 8.388514099061501]
;;  ...
;;  [Timestamp('2017-01-17 00:00:00') 7.186504354691647 7.597836987450301
;;   ... 0.0 0.0 8.318929898087168]
;;  [Timestamp('2017-01-18 00:00:00') 7.18547676307155 7.496134175329733 ...
;;   0.0 0.0 8.151543221567003]
;;  [Timestamp('2017-01-19 00:00:00') 7.184449171451455 7.447042010204286
;;   ... 0.0 0.0 8.163477149645047]]


(plot/with-show
  (py. m plot forecast))

(plot/with-show
  (py. m plot_components forecast))
