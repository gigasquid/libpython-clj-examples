(ns gigasquid.pygal.core
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
;;; sudo pip3 install pygal lxml cairosvg tinycss cssselect

(require-python '[pygal :as pygal])
;;=> :ok

;; http://www.pygal.org/en/latest/documentation/first_steps.html#

;; For list of configuration see
;; http://www.pygal.org/en/latest/documentation/configuration/chart.html
(def config (pygal/Config
             :pretty_print true
             :title "My Pygal Chart"))


(comment
  ;; Some configurable settings
  (py.- config title)            ;;=> "My Pygal Chart"
  (py.- config width)            ;;=> 800
  (py.- config height)           ;;=> 600
  (py.- config show_legend)      ;;=> true
  (py.- config fill)             ;;=> false
  (py.- config style)            ;;=> pygal.style.Style
  (py.- config legend_at_bottom) ;;=> false
  (py.- config legend_box_size)  ;;=> 12
  (py.- config margin)           ;;=> 20
  (py.- config max_scale)        ;;=> 16
  (py.- config min_scale)        ;;=> 4
  (py.- config pretty_print)     ;;=> true

  ;; For full list of options try
  (-> config
      att-type-map
      pprint)

  ;;=> see the useful list in your REPL
  #_
  {
   "__call__" :method,
   "__class__" :meta-config,
   ;; ...
   "_update" :method,
   "allow_interruptions" :bool,
   "box_mode" :str,
   "classes" :list,
   "copy" :method,
   "css" :list,
   "defs" :list,
   "disable_xml_declaration" :bool,
   "dots_size" :float,
   "dynamic_print_values" :bool,
   "explicit_size" :bool,
   "fill" :bool,
   "force_uri_protocol" :str,
   "formatter" :none-type,
   "half_pie" :bool,
   "height" :int,
   "include_x_axis" :bool,
   "inner_radius" :int,
   "interpolate" :none-type,
   "interpolation_parameters" :dict,
   "interpolation_precision" :int,
   "inverse_y_axis" :bool,
   "js" :list,
   "legend_at_bottom" :bool,
   "legend_at_bottom_columns" :none-type,
   "legend_box_size" :int,
   "logarithmic" :bool,
   "margin" :int,
   "margin_bottom" :none-type,
   "margin_left" :none-type,
   "margin_right" :none-type,
   "margin_top" :none-type,
   "max_scale" :int,
   "min_scale" :int,
   "missing_value_fill_truncation" :str,
   "no_data_text" :str,
   "no_prefix" :bool,
   "order_min" :none-type,
   "pretty_print" :bool,
   "print_labels" :bool,
   "print_values" :bool,
   "print_values_position" :str,
   "print_zeroes" :bool,
   "range" :none-type,
   "rounded_bars" :none-type,
   "secondary_range" :none-type,
   "show_dots" :bool,
   "show_legend" :bool,
   "show_minor_x_labels" :bool,
   "show_minor_y_labels" :bool,
   "show_only_major_dots" :bool,
   "show_x_guides" :bool,
   "show_x_labels" :bool,
   "show_y_guides" :bool,
   "show_y_labels" :bool,
   "spacing" :int,
   "stack_from_top" :bool,
   "strict" :bool,
   "stroke" :bool,
   "stroke_style" :none-type,
   "style" :type,
   "title" :str,
   "to_dict" :method,
   "tooltip_border_radius" :int,
   "tooltip_fancy_mode" :bool,
   "truncate_label" :none-type,
   "truncate_legend" :none-type,
   "value_formatter" :default,
   "width" :int,
   "x_label_rotation" :int,
   "x_labels" :none-type,
   "x_labels_major" :none-type,
   "x_labels_major_count" :none-type,
   "x_labels_major_every" :none-type,
   "x_title" :none-type,
   "x_value_formatter" :default,
   "xrange" :none-type,
   "y_label_rotation" :int,
   "y_labels" :none-type,
   "y_labels_major" :none-type,
   "y_labels_major_count" :none-type,
   "y_labels_major_every" :none-type,
   "y_title" :none-type,
   "zero" :int}
  )

;; For bar-chart

(comment

  ;; http://www.pygal.org/en/latest/documentation/configuration/chart.html
  (def barchart (pygal/Bar))

  (py. barchart add "Fibonacci" [0 1 1 2 3 5 8 13 21 34 55])
  ;;=> <pygal.graph.bar.Bar object at 0x12709df50>

  (py. barchart add "Padovan" [1 1 1 2 2 3 4 5 7 9 12])
  ;;=> <pygal.graph.bar.Bar object at 0x12709df50>

  ;; Render will just return the object
  (def result (py. barchart render))

  (type result)
  ;;=> :pyobject

  ;; To render the result in the browser try
  (py. barchart render_in_browser)

  ;; To render the result to file (svg)
  (py. barchart render_to_file "bar_chart.svg")
  ;;=> you should have the file on your system

  ;; To render the result as png
  (py. barchart render_to_png "bar_chart.png")
  ;;=> You should have the file on your system

  )

;; As we may like to try out different flavor of graph
;; Let's create simple function to make it easier to explore.

(defn pg-plot
  "Plot a specific type of graph using Pygal.

  Examples:
  (pg-plot (pygal/Bar :show_legend true
                      :title \"Pygal Bar Chart\"
                      :x_title \"x title\"
                      :y_title \"y title\"
                      :fill true)
           \"Fibonacci\" [0 1 1 2 3 5 8 13 21 34 55]
           \"Padovan\" [1 1 1 2 2 3 4 5 7 9 12])"
  [graph & xs]
  (let [tmp-file (File/createTempFile "tmp-output" ".svg")
        output (.getAbsolutePath tmp-file)]
    (doseq [[x y]
            (partition 2 xs)]
      (py. graph add x y))
    (py. graph render_to_file output)
    (sh/sh "open" output)
    (.deleteOnExit tmp-file)))

(comment
  ;; Simple bar-graph
  (pg-plot (pygal/Bar :show_legend true
                      :title "Bar Chart Example"
                      :x_title "x title"
                      :y_title "y title"
                      :fill true)
           "Fibonacci" [0 1 1 2 3 5 8 13 21 34 55]
           "Padovan" [1 1 1 2 2 3 4 5 7 9 12])

  ;; Simple line-graph
  (pg-plot (pygal/Line :show_legend true
                       :title "Line Chart Example")
           "Fibonacci" [0 1 1 2 3 5 8 13 21 34 55]
           "Padovan" [1 1 1 2 2 3 4 5 7 9 12])

  ;; http://www.pygal.org/en/latest/documentation/types/histogram.html
  (pg-plot (pygal/Histogram :show_legend true
                            :title "Histogram Example")
           "Wide Bars" [[5 0 10]
                        [4 5 13]
                        [2 0 15]]
           "Narrow Bars" [[10 1 2]
                          [12 4 4.5]
                          [8 11 13]])
  )

;; XY - http://www.pygal.org/en/latest/documentation/types/xy.html

(comment
  ;; Basic
  (py/from-import math cos)

  (map (fn [x] [(cos (/ x 10.0)) (/ x 10.0)]) (range -50 50 5))
  (map (fn [x] [(/ x 10.0) (cos (/ x 10.0))]) (range -50 50 5))

  ;; ## Python Code:
  ;; from math import cos
  ;; xy_chart = pygal.XY()
  ;; xy_chart.title = 'XY Cosinus'
  ;; xy_chart.add('x = cos(y)', [(cos(x / 10.), x / 10.) for x in range(-50, 50, 5)])
  ;; xy_chart.add('y = cos(x)', [(x / 10., cos(x / 10.)) for x in range(-50, 50, 5)])
  ;; xy_chart.add('x = 1',  [(1, -5), (1, 5)])
  ;; xy_chart.add('x = -1', [(-1, -5), (-1, 5)])
  ;; xy_chart.add('y = 1',  [(-5, 1), (5, 1)])
  ;; xy_chart.add('y = -1', [(-5, -1), (5, -1)])

  ;; ## Clojure Code - beautiful first class function, compare to Python's list comprehension!
  ;; ## I am obviously bias :)
  (pg-plot (pygal/XY :title "XY Cosinus Example")
           "x = cos(y)" (map (fn [x] [(cos (/ x 10.0)) (/ x 10.0)]) (range -50 50 5))
           "y = cos(x)" (map (fn [x] [(/ x 10.0) (cos (/ x 10.0))]) (range -50 50 5))
           "x = 1" [[-1 -5] [1 5]]
           "x = -1" [[-1 -5] [-1 5]]
           "y = 1" [[-5 1] [5 1]]
           "y = -1" [[-5 -1] [5 -1]])

  ;; Scatter Plot
  (pg-plot (pygal/XY :stroke false
                     :title "Correlation")
           "A" [[0 0] [0.1 0.2] [0.3 0.1] [0.5 1.0] [0.8 0.6] [1.0 1.08] [1.3 1.1] [2, 3.23] [2.43, 2]]
           "B" [[0.1 0.15] [0.12 0.23] [0.4 0.3] [0.6 0.4] [0.21 0.21] [0.5 0.3] [0.6 0.8]
                [0.7 0.8]]
           "C" [[0.05 0.01] [0.13 0.02] [1.5 1.7] [1.52 1.6] [1.8 1.63] [1.5 1.82] [1.7 1.23] [2.1 2.23] [2.3 1.98]]
           )

  ;; Time

  (py/from-import datetime)
  ;; DateTime
  (pg-plot (pygal/DateTimeLine
            :title "DateTime Example"
            :x_label_rotation 35
            :truncate_label -1)
           "Series" [[(datetime 2013 1 2 12 0) 300]
                     [(datetime 2013 1 12 14 30 45) 412]
                     [(datetime 2013 2 2 6) 823]
                     [(datetime 2013 2 22 9 45) 672]])

  ;; Date
  (py/from-import datetime date)
  (pg-plot (pygal/DateLine
            :title "Date Example"
            :x_label_rotation 25
            :x_labels [(date 2013 1 1)
                       (date 2013 7 1)
                       (date 2014 1 1)
                       (date 2015 1 1)
                       (date 2015 7 1)])
           "Series" [[(date 2013 1 2) 213]
                     [(date 2013 8 2) 281]
                     [(date 2014 12 7) 198]
                     [(date 2015 3 21) 120]])

  ;; Time
  (py/from-import datetime time)
  (pg-plot (pygal/TimeLine
            :title "Time Example"
            :x_label_rotation 25)
           "Series" [[(time) 0]
                     [(time 6) 5]
                     [(time 8 30) 12]
                     [(time 11 59 59) 4]
                     [(time 18) 10]
                     [(time 23 30) -1]])

  ;; TimeDelta
  (py/from-import datetime timedelta)

  (pg-plot (pygal/TimeDeltaLine
            :title "Time Delta Example"
            :x_label_rotation 25)
           "Series" [[(timedelta) 0]
                     [(timedelta :seconds 6) 5]
                     [(timedelta :minutes 11 :seconds 59) 4]
                     [(timedelta :days 3 :microseconds 30) 12]
                     [(timedelta :weeks 1) 10]])

  )

;; Pie
;; http://www.pygal.org/en/latest/documentation/types/pie.html#
(comment
  ;; Basic
  (pg-plot (pygal/Pie :show_legend true
                      :title "Browser usage in Feb 2012 (in %)")
           "IE" 19.5
           "Chrome" 36.3
           "Safari" 4.5
           "Opera" 2.3)

  ;; Multi-series
  (pg-plot (pygal/Pie :show_legend true
                      :title "Browser usage in Feb 2012 (in %)")
           "IE" [5.7 10.2 2.6 1]
           "Firefox" [0.6 16.8 7.4 2.2 1.2 1 1 1.1 4.3 1]
           "Chrome" [0.3 0.9 17.1 15.3 0.6 0.5 1.6]
           "Safari" [4.4 0.1]
           "Opera" [0.1 1.6 0.1 0.5])

  ;; Donut
  (pg-plot (pygal/Pie
            :inner_radius 0.4
            :show_legend true
            :title "Browser usage in Feb 2012 (in %)")
           "IE" 19.5
           "Chrome" 36.3
           "Safari" 4.5
           "Opera" 2.3)

  ;; Or a ring
  (pg-plot (pygal/Pie
            :inner_radius 0.75
            :show_legend true
            :title "Browser usage in Feb 2012 (in %)")
           "IE" 19.5
           "Chrome" 36.3
           "Safari" 4.5
           "Opera" 2.3)

  ;; Or Half pie
  (pg-plot (pygal/Pie
            :half_pie true
            :show_legend true
            :title "Browser usage in Feb 2012 (in %)")
           "IE" 19.5
           "Chrome" 36.3
           "Safari" 4.5
           "Opera" 2.3)

  )

;; Radar http://www.pygal.org/en/latest/documentation/types/radar.html
(comment

  (pg-plot (pygal/Radar
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])

  )

;; Box - http://www.pygal.org/en/latest/documentation/types/box.html#extremes-default
(comment

  ;; Extreme (defaul)
  (pg-plot (pygal/Box
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])

  ;; Extreme (Interquartile range)
  (pg-plot (pygal/Box
            :box_mode "1.5IQR"
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])

  ;; Tukey
  (pg-plot (pygal/Box
            :box_mode "tukey"
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])

  ;; Standard deviation
  (pg-plot (pygal/Box
            :box_mode "stdev"
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])

  ;; Population Standard Deviation
  (pg-plot (pygal/Box
            :box_mode "pstdev"
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])

  )

;; Dot - http://www.pygal.org/en/latest/documentation/types/dot.html#

(comment
  ;; Basic
  (pg-plot (pygal/Dot
            :x_label_rotation 30
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])

  ;; Negative
  (pg-plot (pygal/Dot
            :x_label_rotation 30)
           "Normal" [10 50 76 80 25]
           "With negatives" [0 -34 -29 39 -75]
           )

  )

;; Funnel - http://www.pygal.org/en/latest/documentation/types/funnel.html
(comment

  ;; Basic
  (pg-plot (pygal/Funnel
            :title "V8 Benchmark Results"
            :x_labels ["Richards"
                       "DeltaBlue"
                       "Crypto"
                       "RayTrace"
                       "EarleyBoyer"
                       "RegExp"
                       "Splay"
                       "NavierStokes"])
           "Chrome", [6395, 8212, 7520, 7218, 12464, 1660, 2123, 8607]
           "Firefox", [7473, 8099, 11700, 2651, 6361, 1044, 3797, 9450]
           "Opera", [3472, 2933, 4203, 5229, 5810, 1828, 9013, 4669]
           "IE", [43, 41, 59, 79, 144, 136, 34, 102])
  )

;;SolidGuage - http://www.pygal.org/en/latest/documentation/types/solidgauge.html

(comment

  ;; Normal
  (pg-plot (pygal/SolidGauge
            :inner_radius 0.70
            :title "Solid Guage Normal Example"
            :value_formatter (fn [x] (format "%s %%" x)))
           "Series 1" [{:value 225000 :max_value 1275000}]
           "Series 2" [{:value 110 :max_value 100}]
           "Series 3" [{:value 3}]
           "Series 4" [{:value 51 :max_value 100}
                       {:value 12 :max_value 100}]
           "Series 5" [{:value 79 :max_value 100}]
           "Series 6" [{:value 99}]
           "Series 7" [{:value 100 :max_value 100}])


  ;; Half
  (let [style (py.- (pygal/Config :value_font_size 10) style)]
    (pg-plot (pygal/SolidGauge :half_pie true
                               :inner_radius 0.70
                               :title "Solid Guage Half Example"
                               :style style
                               :value_formatter (fn [x] (format "%s %%" x)))
             "Series 1" [{:value 225000 :max_value 1275000}]
             "Series 2" [{:value 110 :max_value 100}]
             "Series 3" [{:value 3}]
             "Series 4" [{:value 51 :max_value 100}
                         {:value 12 :max_value 100}]
             "Series 5" [{:value 79 :max_value 100}]
             "Series 6" [{:value 99}]
             "Series 7" [{:value 100 :max_value 100}]))

  )

;; TODO: add example for Pyramid
;; http://www.pygal.org/en/latest/documentation/types/pyramid.html

;; Treemap
;; http://www.pygal.org/en/latest/documentation/types/treemap.html
(comment
  (pg-plot (pygal/Treemap
            :title "Binary Treemap Example")
           "A" [2, 1, 12, 4, 2, 1, 1, 3, 12, 3, 4, nil, 9]
           "B" [4, 2, 5, 10, 3, 4, 2, 7, 4, -10, nil, 8, 3, 1]
           "C" [3, 8, 3, 3, 5, 3, 3, 5, 4, 12]
           "D" [23, 18]
           "E" [1, 2, 1, 2, 3, 3, 1, 2, 3,4, 3, 1, 2, 1, 1, 1, 1, 1]
           "F" [31]
           "G" [5, 9.3, 8.1, 12, 4, 3, 2]
           "H" [12, 3, 3])
  )

;; Maps
(comment
  ;; Require
  ;; pip install pygal_maps_world
  (require-python '[pygal_maps_world :as pygal-mw])
  ;; TODO: continue world map example
  )

;; http://www.pygal.org/en/latest/documentation/types/maps/index.html
;; - World Map
;; - French Map
;;  - Department
;;  - Regions
;;  - Department list
;;  - Region list
;; - Swiss Map
;;  - Canton
;;  - Canton list
