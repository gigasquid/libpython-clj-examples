(ns gigasquid.opencv.core
  (:require
   [clojure.string :as string]
   [libpython-clj.require
    :refer [require-python]]
   [libpython-clj.python
    :as py
    :refer [py.
            py..
            py.-
            att-type-map
            ->python
            ->jvm
            as-map
            as-list]]
   [clojure.java.shell :as sh]
   [clojure.pprint :refer [pprint]])
  (:import [java.io File]))

;;; Python installation
;;; sudo pip3 install numpy matplotlib opencv-contrib-python-headless

(require-python
 '[cv2
   :as cv2]
 '[matplotlib.pyplot
   :as pyplot]
 '[builtins
   :as python
   :refer [slice tuple]]
 '[numpy
   :as np
   :refer [array]]
 '[operator
   :as operator
   :refer [getitem]])

;; ====================================== ;;
;; Basic exploration to learn the api
;; ====================================== ;;
(comment

  (def img (cv2/imread "resources/opencv/opencv-logo.png"))

  (-> img
      att-type-map)

  ;; Note: how we de-structure Python's tuple to vector in Clojure
  (let [img (cv2/imread "resources/opencv/opencv-logo.png")
        [h w c] (py.- img shape)]
    [h w c])
  ;;=> (99, 82, 3)

  ;; Total number of elements is obtained by img.size
  (py.- img size) ;;=> 24354

  (py.- img dtype) ;;=> uint8

  (def img2 (cv2/cvtColor img cv2/COLOR_BGR2GRAY))

  ;; Save the result to the file
  (cv2/imwrite "resources/opencv/opencv-gray-logo.png" img2) ;;=> true

  )

;; ====================================== ;;
;; Useful transformation function
;; ====================================== ;;
(defn ^:private read-input
  [input-file]
  (let [input-image (cv2/imread input-file)
        temp-file (File/createTempFile "opencv-temp" ".png")]
    [input-image temp-file]))

;; sketch image
(defn sketch-image
  [img]
  (let [img-gray (cv2/cvtColor img cv2/COLOR_BGR2GRAY)
        img-gray (cv2/medianBlur img-gray 5)
        edges (cv2/Laplacian img-gray cv2/CV_8U :ksize 5)
        [_ thresholded] (cv2/threshold edges 70 255 cv2/THRESH_BINARY_INV)]
    thresholded))

(comment
  ;; Sketch the cat image
  (let [img (cv2/imread "resources/opencv/cat.jpg")]
    (sketch-image img))

  )

(defn cartoonize-image
  ([image]
   (cartoonize-image image false))
  ([image gray-mode]
   (let [thresholed (sketch-image image)
         filtered (cv2/bilateralFilter image 10 250 250)
         cartoonized (cv2/bitwise_and filtered filtered :mask thresholed)]
     (if gray-mode
       (cv2/cvtColor cartoonized cv2/COLOR_BGR2GRAY)
       cartoonized))))

(defn cartoonize-image-gray
  [image]
  (cartoonize-image image true))

(comment
  ;; gray-mode true
  (let [image (cv2/imread "resources/opencv/cat.jpg")]
    (cartoonize-image image true))

  ;; Or use the wrapper function
  (let [image (cv2/imread "resources/cat.jpg")]
    (cartoonize-image-gray image))

  ;; color mode
  (let [image (cv2/imread "resources/cat.jpg")]
    (cartoonize-image image))
  )

;; Re-usable function for exercising the above functions
(defn process-image
  "Apply opencv function to a given image and optionally show it.

  (process-image {:input-file \"resources/opencv/cat.jpg\"
                  :output-file \"resources/opencv/cat-sketch.png\"
                  :xform-fns sketch-image
                  :open? true})"
  [& [{:keys [input-file
              output-file
              tx-fns
              open?]
       :or {input-file "resources/opencv/cat.jpg"
            open? true}}]]
  (pyplot/figure :figsize (python/tuple [14 6]))
  (pyplot/suptitle "Example Sketch"
                   :fontsize 14
                   :fontweight "bold")
  (let [image-src (cv2/imread input-file)
        ;; TODO: allow arguments to the function to avoid overload
        image-dst (tx-fns image-src)]
    (cv2/imwrite output-file image-dst)
    (if open?
      (sh/sh "open" output-file)
      (println (format "Your output file : %s" output-file)))))

(comment

  ;; Process image as sketch
  (process-image {:input-file "resources/opencv/cat.jpg"
                  :output-file "resources/opencv/cat-sketch.png"
                  :tx-fns sketch-image})

  (process-image {:input-file "resources/opencv/cat.jpg"
                  :output-file "resources/opencv/cat-cartoonize-color.png"
                  :tx-fns cartoonize-image})

  (process-image {:input-file "resources/opencv/cat.jpg"
                  :output-file "resources/opencv/cat-cartoonize-gray.png"
                  :tx-fns cartoonize-image-gray})

  )

(defn -main
  [& args]
  (process-image {:input-file "resources/opencv/cat.jpg"
                  :output-file "resources/opencv/cat-sketch.png"
                  :tx-fns sketch-image}))

;; We can also run it via main function
#_(-main)
