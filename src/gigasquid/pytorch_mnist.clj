;; This example was ported from pytorch/examples MNIST from:
;; https://github.com/pytorch/examples.git
(ns gigasquid.pytorch-mnist
  (:require
   [libpython-clj.python :as py
    :refer [py* py** py. py.. py.- $a $.
            as-jvm with-gil-stack-rc-context
            stack-resource-context
            import-module
            get-attr get-item att-type-map call call-attr]]
   [libpython-clj.require :refer [require-python]]))

;;; sudo pip3 install torch
;;; sudo pip3 install torchvision

(require-python
 '[torch :as torch]
 '[torch.cuda :as cuda]
 '[torch.onnx :as onnx]
 '[torch.nn :as nn :refer [Conv2d Dropout2d Linear]]
 '[torch.optim :as optim]
 '[torch.utils.data :as tud]
 '[torch.nn.functional :as F]
 '[torchvision.datasets :as datasets]
 '[torchvision.transforms :as transforms]
 '[torch.optim.lr_scheduler :as lr_scheduler])

(def enumerate (-> (py/import-module "builtins")
                   (get-attr "enumerate")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; If you have CUDA but do not want to use it, set this to false
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def ^:dynamic *use-cuda* (cuda/is_available))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def log-interval 100)

;; Yann LeCun:
;; Training with large minibatches is bad for your health.
;; More importantly, it's bad for your test error.
;; Friends dont let friends use minibatches larger than 32. 
;; https://twitter.com/ylecun/status/989610208497360896
;;
;; input batch size for training (default: 64)
(def batch-size 32)
;; input batch size for testing (default: 1000)
(def test-batch-size 1000)
;; number of epochs to train (default: 14)
(def epochs 14)
;; learning rate (default: 1.0)
(def learning-rate 1.0)
;; Learning rate step gamma (default: 0.7)
(def gamma 0.7)
;; random seed (default: 1)
(def seed 42)

(def mnist-mean [0.1307])
(def mnist-std [0.3081])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce device (atom nil))
(defonce train-data (atom nil))
(defonce train-loader (atom nil))
(defonce test-data (atom nil))
(defonce test-loader (atom nil))
(defonce model (atom nil))
(defonce optimizer (atom nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; load MNIST data from the internet
(defn load-data! []
  (let [gpu-opts (if *use-cuda*
                   {:num_workers 1 :pin_memory true}
                   {})
        mnist-transform (transforms/Compose
                         [(transforms/ToTensor)
                          (transforms/Normalize mnist-mean mnist-std)])]
    ;; training data and loader
    (reset! train-data
            (datasets/MNIST "./resources/pytorch/data"
                            :train true :download true :transform mnist-transform))
    (let [kwargs (merge {:batch_size batch-size :shuffle true}
                        gpu-opts)
          args (into [@train-data] (mapcat identity kwargs))]
      (reset! train-loader (apply tud/DataLoader args)))

    ;; test data and loader
    (reset! test-data
            (datasets/MNIST "./resources/pytorch/data"
                            :train false :download true :transform mnist-transform))
    (let [kwargs (merge {:batch_size test-batch-size :shuffle true}
                        gpu-opts)
          args (into [@test-data] (mapcat identity kwargs))]
      (reset! test-loader (apply tud/DataLoader args))))

  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;; neural network definition, uses convolutional neural nets (CNNs)
(def MyNet
  (py/create-class
   "MyNet" [nn/Module]
   {"__init__"
    (py/make-tuple-instance-fn
     (fn [self]
       (py. nn/Module __init__ self)
       (py/set-attrs!
        self
        {"conv1" (Conv2d 1 32 3 1)
         "conv2" (Conv2d 32 64 3 1)
         "dropout1" (Dropout2d 0.25)
         "dropout2" (Dropout2d 0.5)
         "fc1" (Linear 9216 128)
         "fc2" (Linear 128 10)})

       ;; __init__ must return nil
       nil))
    "forward"
    (py/make-tuple-instance-fn
     (fn [self x]
       (let [x (py. self conv1 x)
             x (F/relu x)
             x (py. self conv2 x)
             x (F/max_pool2d x 2)
             x (py. self dropout1 x)
             x (torch/flatten x 1)
             x (py. self fc1 x)
             x (F/relu x)
             x (py. self dropout2 x)
             x (py. self fc2 x)
             output (F/log_softmax x :dim 1)]
         output))
     :arg-converter as-jvm
     :method-name "forward")}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn setup! []
  (py/gc!)
  (torch/manual_seed seed)
  (reset! device (if *use-cuda*
                   (torch/device "cuda")
                   (torch/device "cpu")))
  (load-data!)
  (reset! model
          (let [inst (MyNet)]
            (py. inst "to" @device)))
  (reset! optimizer
          (optim/Adadelta (py. @model "parameters")
                          :lr learning-rate))
  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn train [args model device train-loader optimizer epoch]
  (py. model train)
  (dorun
   (for [[batch-idx [data target]] (enumerate train-loader)]
     (with-gil-stack-rc-context
       (let [data (py. data to device)
             target (py. target to device)]
         (py. optimizer zero_grad)
         (let [output (py. model __call__ data)
               loss (F/nll_loss output target)]
           (py. loss backward)
           (py. optimizer step)
           (when (= 0 (mod batch-idx log-interval))
             (println
              (format "Train Epoch: %d [%d/%d (%.1f%%)]\tLoss: %.6f"
                      epoch
                      (* batch-idx (int (py. data "__len__")))
                      (py. (py.- train-loader dataset) "__len__")
                      (/ (* 100.0 batch-idx) (int (py. train-loader "__len__")))
                      (py. loss item))))))))))

(defn test-model [args model device test-loader]
  (py. model eval)
  (let [test-lost (atom 0)
        correct (atom 0)]
    (letfn [(test-batch [data target]
              (let [data (py. data to device)
                    target (py. target to device)
                    output (py. model __call__ data)]
                (swap! test-lost +
                       (py. (F/nll_loss output target :reduction "sum") item))
                (let [pred (py. output argmax :dim 1 :keepdim true)]
                  (swap! correct +
                         (-> (py. pred eq (py. target view_as pred))
                             (py. sum)
                             (py. item))))))]

      ; pytorch crash with "python error in flight"
      ; (py/with [ng torch/no_grad]
      ;   (dorun
      ;    (for [[data target] test-loader]
      ;      (with-gil-stack-rc-context
      ;        (test-batch data target)))))

      ; pytorch crash with "python error in flight"
      ; (py/with [ng torch/no_grad]
      ;   (dorun
      ;    (for [[data target] test-loader]
      ;      (stack-resource-context
      ;       (test-batch data target)))))

      (let [no-grad (torch/no_grad)]
        (try
         (py. no-grad __enter__)
         (dorun
          (for [[data target] test-loader]
            (with-gil-stack-rc-context
              (test-batch data target))))
         (finally
          (py. no-grad __exit__)))))

    (let [data-set (py.- test-loader dataset)
          n (py. data-set __len__)]
      (swap! test-lost / (py. data-set __len__))
      (println
       (format "\nTest set: Average loss: %.4f, Accuracy %d/%d (%.1f%%)\n"
               @test-lost @correct
               n
               (/ (* 100. @correct) (int n)))))))

(defn train-test-loop!
  "RUN THIS IN A CONSOLE REPL IF YOUR EDITOR REPL DOESN'T HAVE STREAMING"
  []
  (let [scheduler (lr_scheduler/StepLR @optimizer :step_size 1 :gamma gamma)
        args {}]
    (dorun
     (for [epoch (range epochs)]
       (do
         (train args @model @device @train-loader @optimizer epoch)
         (test-model args @model @device @test-loader)
         (py. scheduler step))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; save the model to the universal ONNX format
;;; you can use NETRON at https://github.com/lutzroeder/netron to visualize
;;; this model. 
(defn save-model! []
  (let [tensor (first (first (seq @train-loader)))
        size (vec (py. tensor size))
        args (into size [:device "cuda"])
        dummy-input (apply torch/randn args)]
    (onnx/export @model dummy-input "resources/pytorch/models/mnist_cnn.onnx"
                 :verbose true
                 :output_names ["digit_from_0_to_9"])
    nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn --profile-cuda []
  (binding [*use-cuda* true]
    (setup!)
    (train-test-loop!)))

(defn --profile-no-cuda []
  (binding [*use-cuda* false]
    (setup!)
    (train-test-loop!)))

(comment
  (setup!)
  (train-test-loop!)
  (save-model!))
