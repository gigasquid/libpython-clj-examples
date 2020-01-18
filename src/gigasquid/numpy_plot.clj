(ns gigasquid.numpy-plot
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [gigasquid.plot :as plot]))

;;; Try printing from another namespace

(require-python 'matplotlib.pyplot)
(require-python 'numpy)

(comment 
  (def x (numpy/linspace 0 2 50))

  (plot/with-show (matplotlib.pyplot/plot [[1 2 3 4 5] [1 2 3 4 10]] :label "linear"))

  (plot/with-show
    (matplotlib.pyplot/plot [x x] :label "linear")
    (matplotlib.pyplot/plot [x (py. x "__pow__" 2)] :label "quadratic")
    (matplotlib.pyplot/plot [x (py. x "__pow__" 3)] :label "cubic")
    (matplotlib.pyplot/xlabel "x label")
    (matplotlib.pyplot/ylabel "y label")
    (matplotlib.pyplot/title "Simple Plot"))


  ;;; numpy printing tutorial http://cs231n.github.io/python-numpy-tutorial/#matplotlib-plotting
  (let [x (numpy/arange 0 (* 3 numpy/pi) 0.1)
        y (numpy/sin x)]
    (plot/with-show
      (matplotlib.pyplot/plot x y)))

  (let [x (numpy/arange 0 (* 3 numpy/pi) 0.1)
        y-sin (numpy/sin x)
        y-cos (numpy/cos x)]
    (plot/with-show
      (matplotlib.pyplot/plot x y-sin)
      (matplotlib.pyplot/plot x y-cos)
      (matplotlib.pyplot/xlabel "x axis label")
      (matplotlib.pyplot/ylabel "y axis label")
      (matplotlib.pyplot/title "Sine and Cosine")
      (matplotlib.pyplot/legend ["Sine" "Cosine"])))

  ;;;; Subplots

  (let [x (numpy/arange 0 (* 3 numpy/pi) 0.1)
        y-sin (numpy/sin x)
        y-cos (numpy/cos x)]
    (plot/with-show
      ;;; set up a subplot gird that has a height of 2 and width of 1
      ;; and set the first such subplot as active
      (matplotlib.pyplot/subplot 2 1 1)
      (matplotlib.pyplot/plot x y-sin)
      (matplotlib.pyplot/title "Sine")

      ;;; set the second subplot as active and make the second plot
      (matplotlib.pyplot/subplot 2 1 2)
      (matplotlib.pyplot/plot x y-cos)
      (matplotlib.pyplot/title "Cosine")))

;;;;; Images

  (let [img (matplotlib.pyplot/imread "resources/cat.jpg")
        img-tinted (numpy/multiply img [1 0.95 0.9])]
    (plot/with-show
      (matplotlib.pyplot/subplot 1 2 1)
      (matplotlib.pyplot/imshow img)
      (matplotlib.pyplot/subplot 1 2 2)
      (matplotlib.pyplot/imshow (numpy/uint8 img-tinted)))))


