# OpenCV

- [OpenCV](https://opencv.org/)
- Official OpenCV [documentation](https://opencv.org/)

## Basic installation 

- Python 

```shell
pip3 install numpy matplotlib opencv-contrib-python-headless
```

- Clojure 

Just run your favorite `cider-jack-in` if you are on Emacs.
For other editors, you will do the equivalent command for your editor.

## Outputs

![Input Image](../../../../master/resources/opencv/cat.jpg)

- Simple Sketch 

```clojure
;; Process image as sketch
(process-image {:input-file "resources/opencv/cat.jpg"
                :output-file "resources/opencv/cat-sketch.png"
                :tx-fns sketch-image})
```

![Simple Sketch](../../../../master/resources/opencv/cat-sketch.png)

- Cartoonize Image (color)

```clojure
(process-image {:input-file "resources/opencv/cat.jpg"
                :output-file "resources/opencv/cat-cartoonize-color.png"
                :tx-fns cartoonize-image})
```

![Cartoonize Image (color)](../../../../master/resources/opencv/cat-cartoonize-color.png)

- Cartoonize Image (gray-scale)

```clojure
(process-image {:input-file "resources/opencv/cat.jpg"
                :output-file "resources/opencv/cat-cartoonize-gray.png"
                :tx-fns cartoonize-image-gray})
```

![Cartoonize Image (gray-scale)](../../../../master/resources/opencv/cat-cartoonize-gray.png)
