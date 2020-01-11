(ns gigasquid.mxnet
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py]
            [clojure.string :as string]))

;;; sudo pip3 install mxnet

(require-python '(mxnet mxnet.ndarray mxnet.module mxnet.io))
(require-python '(mxnet.test_utils))
(require-python '(mxnet.initializer))
(require-python '(mxnet.metric))
(require-python '(mxnet.symbol))


;;; get the mnist data and format it

(def mnist (mxnet.test_utils/get_mnist))
(def train-x (mxnet.ndarray/array (py/$a (py/get-item mnist "train_data") "reshape" -1 784)))
(def train-y (mxnet.ndarray/array (py/get-item mnist "train_label")))
(def test-x (mxnet.ndarray/array (py/$a (py/get-item mnist "test_data") "reshape" -1 784)))
(def test-y (mxnet.ndarray/array (py/get-item mnist "test_label")))

(def batch-size 100)

(def train-dataset (mxnet.io/NDArrayIter :data train-x
                                         :label train-y
                                         :batch_size batch-size
                                         :shuffle true))
(def test-dataset (mxnet.io/NDArrayIter :data test-x
                                        :label test-y
                                        :batch_size batch-size))


(def data-shapes (py/get-attr train-dataset "provide_data"))
(def label-shapes (py/get-attr train-dataset "provide_label"))

data-shapes ;=>  [DataDesc[data,(10, 784),<class 'numpy.float32'>,NCHW]]
label-shapes ;=> [DataDesc[softmax_label,(10,),<class 'numpy.float32'>,NCHW]]


;;;; Setting up the model and initializing it

(def data (mxnet.symbol/Variable "data"))

(def net (-> (mxnet.symbol/Variable "data")
             (mxnet.symbol/FullyConnected :name "fc1" :num_hidden 128)
             (mxnet.symbol/Activation :name "relu1" :act_type "relu")
             (mxnet.symbol/FullyConnected :name "fc2" :num_hidden 64)
             (mxnet.symbol/Activation :name "relu2" :act_type "relu")
             (mxnet.symbol/FullyConnected :name "fc3" :num_hidden 10)
             (mxnet.symbol/SoftmaxOutput :name "softmax")))



(def model (py/call-kw mxnet.module/Module [] {:symbol net :context (mxnet/cpu)}))
(py/$a model bind :data_shapes data-shapes :label_shapes label-shapes)
(py/$a model init_params)
(py/$a model init_optimizer :optimizer "adam")
(def acc-metric (mxnet.metric/Accuracy))


(defn end-of-data-error? [e]
  (string/includes? (.getMessage e) "StopIteration"))

(defn reset [iter]
  (py/$a iter reset))

(defn next-batch [iter]
  (try (py/$a iter next)
       (catch Exception e
         (when-not (end-of-data-error? e)
           (throw e)))))

(defn get-metric [metric]
  (py/$a metric get))

(defn train-epoch [model dataset metric]
  (reset dataset)
  (loop [batch (next-batch dataset)
         i 0]
    (if batch
      (do
        (py/$a model forward batch :is_train true)
        (py/$a model backward)
        (py/$a model update)
        (py/$a model update_metric metric (py/get-attr batch "label"))
        (when (zero? (mod i 100)) (println "i-" i " Training Accuracy " (py/$a metric get)))
        (recur (next-batch dataset) (inc i)))
      (println "Final Training Accuracy " (get-metric metric)))))

(defn test-accuracy [model dataset metric]
  (reset dataset)
  (loop [batch (next-batch dataset)
         i 0]
    (if batch
      (do
        (py/$a model forward batch)
        (py/$a model update_metric metric (py/get-attr batch "label"))
        (when (zero? (mod i 100)) (println "i-" i " Test Accuracy " (py/$a metric get)))
        (recur (next-batch dataset) (inc i)))
      (println "Final Test Accuracy " (get-metric metric)))))


(comment 


  ;;;training
  (dotimes [i 3]
    (println "========= Epoch " i  " ============")
    (train-epoch model train-dataset acc-metric))
  (get-metric acc-metric) ;=> ('accuracy', 0.9483555555555555)

  ;;;;
  (test-accuracy model test-dataset acc-metric)
  (get-metric acc-metric) ;=> ('accuracy', 0.9492052631578948)

  ;;visualization

  (def bd (next-batch train-dataset))
  (def data (first (py/get-attr bd "data")))

  (def image (mxnet.ndarray/slice data :begin 0 :end 1))
  (def image2 (py/$a image "reshape" [28 28]))
  (def image3 (-> (mxnet.ndarray/multiply image2 256)
                  (mxnet.ndarray/cast :dtype "uint8")))
  (def npimage (py/$a image3 asnumpy))


  (require-python '(cv2))
  (cv2/imwrite "number.jpg" npimage)


  )






