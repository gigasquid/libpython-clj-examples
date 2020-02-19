(ns gigasquid.bokeh.core
  (:require [libpython-clj.require
             :refer [require-python]]
            [libpython-clj.python
             :as py
             :refer [py.
                     py..
                     py.-
                     att-type-map
                     ->python
                     ->jvm
                     ]]
            [clojure.java.shell :as sh]
            [clojure.pprint :refer [pprint]])
  (:import [java.io File]))

;;; Python installation
;;; pip3 install bokeh

(comment
  (require-python '[sklearn.svm :as svm])
  (require-python '[numpy :as np])
  #_ (require-python '[pandas :as pd])
  )

;; First require the basic package
(require-python '[bokeh.plotting :as bkp])

(comment

  ;; Quick getting start guide
  ;; https://docs.bokeh.org/en/latest/docs/user_guide/quickstart.html#getting-started
  (py/from-import bokeh.plotting figure output_file show curdoc)

  (let [x [1 2 3 4 5]
        y [6 7 2 4 5]
        p (bkp/figure :title "Simple line example"
                      :x_axis_label "x"
                      :y_axis_label "y")]
    (py. p line x y :legend "Temp." :line_width 2)
    (bkp/show p))

  ;; More plotting example
  (let [x [0.1, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0]
        y0 (into [] (map (fn [i] (Math/pow i 2)) x))
        y1 (into [] (map (fn [i] (Math/pow 10 i)) x))
        y2 (into [] (map (fn [i] (Math/pow 10 (Math/pow i 2))) x))
        p (bkp/figure :tools "pan,box_zoom,reset,save"
                      :y_axis_type "log"
                      :y_range [0.001 (Math/pow 10 11)]
                      :title "log axis example"
                      :x_axis_label "sections"
                      :y_axis_label "particles")]
    (py. p line x x
         :legend "y=x")

    (py. p circle x x
         :legend "y=x"
         :fill_color "white"
         :size 8)

    (py. p line x y0
         :legend "y=x^2"
         :line_width 3)

    (py. p line x y1
         :legend "y=10^x"
         :line_color "red")

    (py. p circle x y1
         :legend "y=10^x"
         :fill_color "red"
         :line_color "red"
         :size 6)

    (py. p line x y2
         :legend "y=10^x^2"
         :line_color "orange"
         :line_dash "4 4")

    (bkp/show p))

  )

(comment
  ;; More example
  (let [p (bkp/figure
           :plot_width 300
           :plot_height 300
           :tools "pan,reset,save")]
    (py. p
         circle
         [1 2.5 3   2]
         [2   3 1 1.6]
         :radius 0.3
         :alpha 0.5)
    (bkp/show p))

  )


;; Providing Data
;; https://docs.bokeh.org/en/latest/docs/user_guide/data.html

(comment
  (require-python '[bokeh.plotting :as bkp]) ;;=> :ok
  (require-python '[bokeh.models :as bkm])   ;;=> :ok

  (let [data {:x_values [1 2 3 4 5]
              :y_values [6 7 2 3 6]}
        source (bkm/ColumnDataSource :data data)
        p (bkp/figure)]
    (py. p circle
         :x "x_values"
         :y "y_values"
         :source source)
    (bkp/show p))

  )
