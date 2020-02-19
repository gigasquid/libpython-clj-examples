(ns gigasquid.bokeh.multi-polygons
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

;; First require the basic package
(require-python '[bokeh.plotting :as bkp])

(comment
  ;; https://github.com/bokeh/bokeh/blob/1.4.0/examples/plotting/notebook/MultiPolygons.ipynb

  (py/from-import bokeh.plotting figure output_file show curdoc)

  (let [p (bkp/figure :title "Polygons with no holes"
                      :plot_width 300
                      :plot_height 300
                      :tools "hover,tap,wheel_zoom,pan,reset,help")]
    (py. p multi_polygons
         :xs [[[[1, 2, 2, 1, 1]]]]
         :ys [[[[3, 3, 4, 4, 3]]]])
    (bkp/show p))

  ;; Polygons with holes
  (let [p (bkp/figure :title "Polygons with holes"
                      :plot_width 300
                      :plot_height 300
                      :tools "hover,tap,wheel_zoom,pan,reset,help")]
    (py. p multi_polygons
         :xs [[[[1, 2, 2, 1], [1.2, 1.6, 1.6], [1.8, 1.8, 1.6]]]]
         :ys [[[[3, 3, 4, 4], [3.2, 3.6, 3.2], [3.4, 3.8, 3.8]]]])
    (bkp/show p))

  )

(comment
  ;; https://github.com/bokeh/bokeh/blob/1.4.0/examples/plotting/notebook/MultiPolygons.ipynb
  ;; Now we'll examine a MultiPolygon.
  ;; A MultiPolygon is composed of different parts each of which is a Polygon and each of which can have or not have holes.

  ;; In python:
  ;; p = figure(plot_width=300, plot_height=300, tools='hover,tap,wheel_zoom,pan,reset,help')
  ;; p.multi_polygons(xs=[[[ [1, 1, 2, 2], [1.2, 1.6, 1.6], [1.8, 1.8, 1.6] ], [ [3, 4, 3] ]]],
  ;;                    ys=[[[ [4, 3, 3, 4], [3.2, 3.2, 3.6], [3.4, 3.8, 3.8] ], [ [1, 1, 3] ]]])
  ;; show(p)

  (let [p (bkp/figure
           :title "Multi-Polygons 1"
           :plot_width 300
           :plot_height 300
           :tools "hover,tap,wheel_zoom,pan,reset,help")
        xs [[[[1, 1, 2, 2] [1.2, 1.6, 1.6] [1.8, 1.8, 1.6]] [[3, 4, 3]]]]
        ys [[[[4, 3, 3, 4] [3.2, 3.2, 3.6] [3.4, 3.8, 3.8]] [[1, 1, 3]]]]]
    (py. p multi_polygons :xs xs :ys ys)
    (bkp/show p))

  ;; More example
  (let [p (bkp/figure
           :title "Multi-Polygons 2"
           :plot_width 300
           :plot_height 300
           :tools "hover,tap,wheel_zoom,pan,reset,help")
        xs [[[[1 1 2 2] [1.2 1.6 1.6] [1.8 1.8 1.6]] [[3 3 4]]]
            [[[1 2 2 1] [1.3 1.3 1.7 1.7]]]]
        ys [[[[4 3 3 4] [3.2 3.2 3.6] [3.4 3.8 3.8]] [[1 3 1]]],
            [[[1 1 2 2] [1.3 1.7 1.7 1.3]]]]]
    (py. p multi_polygons :xs xs :ys ys)
    (bkp/show p))


  ;; ===================================== ;;
  ;; Using multi-polygons glyph directly
  ;; TODO: revisit this code
  #_
  (comment
    (py/from-import bokeh.models ColumnDataSource Plot LinearAxis Grid)
    (py/from-import bokeh.models.glyphs MultiPolygons)
    (py/from-import bokeh.models.tools TapTool WheelZoomTool ResetTool HoverTool)
    (py/from-import bokeh.plotting figure output_file show curdoc)

    ;; Note: for this we need to use dict
    (require-python '[builtins :as python])

    (let [source (ColumnDataSource(python/dict
                                   :xs [[[[1, 1, 2, 2]
                                          [1.2, 1.6, 1.6]
                                          [1.8, 1.8, 1.6]]
                                         [[3, 3, 4]]]
                                        [[[1, 2, 2, 1]
                                          [1.3, 1.3, 1.7, 1.7]]]]
                                   :ys [[[[4, 3, 3, 4]
                                          [3.2, 3.2, 3.6]
                                          [3.4, 3.8, 3.8]]
                                         [[1, 3, 1]]]
                                        [[[1, 1, 2, 2]
                                          [1.3, 1.7, 1.7, 1.3]]]]
                                   :color ["blue" "red"],
                                   :label ["A" "B"]))
          plot (Plot :plot_width 300
                     :plot_height 300
                     ;;:tools [(HoverTool) (TapTool) (WheelZoomTool)]
                     )
          glyph (py. MultiPolygons :xs "xs" :ys "ys" :fill_color "color")]
      plot
      #_(py. plot add_glyph source glyph)
      )
    )
  ;; ===================================== ;;

  ;; TODO:
  ;; Using numpy array with MultiPolygons
