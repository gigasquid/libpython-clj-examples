(ns gigasquid.numpy-plot
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))


(require-python '([matplotlib.pyplot :as pyplot]
                  [numpy :as numpy]))

;;;; you will need matplotlib, numpy, and pillow installed to run this in python3


;;; This uses a macro from printing in the plot namespace that uses the shell "open" command
;;; to show a saved image from pyplot. If you don't have a mac you will need to modify that
;;; to whatever shell command you have.

(comment
  (def x (numpy/linspace 0 2 50))

  (plot/with-show (matplotlib.pyplot/plot [[1 2 3 4 5] [1 2 3 4 10]] :label "linear"))

  (plot/with-show
    (pyplot/plot [x x] :label "linear")
    (pyplot/plot [x (py. x "__pow__" 2)] :label "quadratic")
    (pyplot/plot [x (py. x "__pow__" 3)] :label "cubic")
    (pyplot/xlabel "x label")
    (pyplot/ylabel "y label")
    (pyplot/title "Simple Plot"))


  ;;; numpy printing tutorial http://cs231n.github.io/python-numpy-tutorial/#matplotlib-plotting
  (let [x (numpy/arange 0 (* 3 numpy/pi) 0.1)
        y (numpy/sin x)]
    (plot/with-show
      (pyplot/plot x y)))

  (let [x (numpy/arange 0 (* 3 numpy/pi) 0.1)
        y-sin (numpy/sin x)
        y-cos (numpy/cos x)]
    (plot/with-show
      (pyplot/plot x y-sin)
      (pyplot/plot x y-cos)
      (pyplot/xlabel "x axis label")
      (pyplot/ylabel "y axis label")
      (pyplot/title "Sine and Cosine")
      (pyplot/legend ["Sine" "Cosine"])))

  ;;;; Subplots

  (let [x (numpy/arange 0 (* 3 numpy/pi) 0.1)
        y-sin (numpy/sin x)
        y-cos (numpy/cos x)]
    (plot/with-show
      ;;; set up a subplot gird that has a height of 2 and width of 1
      ;; and set the first such subplot as active
      (pyplot/subplot 2 1 1)
      (pyplot/plot x y-sin)
      (pyplot/title "Sine")

      ;;; set the second subplot as active and make the second plot
      (pyplot/subplot 2 1 2)
      (pyplot/plot x y-cos)
      (pyplot/title "Cosine")))

;;;;; Images

  (let [img (pyplot/imread "resources/cat.jpg")
        img-tinted (numpy/multiply img [1 0.95 0.9])]
    (plot/with-show
      (pyplot/subplot 1 2 1)
      (pyplot/imshow img)
      (pyplot/subplot 1 2 2)
      (pyplot/imshow (numpy/uint8 img-tinted))))


;;;;; pie chart
;;;; from https://org/3.1.1/gallery/pie_and_polar_charts/pie_features.html


  (let [labels ["Frogs" "Hogs" "Dogs" "Logs"]
        sizes [15 30 45 10]
        explode [0 0.1 0 0] ; only explode the 2nd slice (Hogs)
        ]
    (plot/with-show
      (let [[fig1 ax1] (pyplot/subplots)]
        (py. ax1 "pie" sizes :explode explode :labels labels :autopct "%1.1f%%"
                             :shadow true :startangle 90)
        (py. ax1 "axis" "equal")) ;equal aspec ration ensures that pie is drawn as circle
      ))
  )


