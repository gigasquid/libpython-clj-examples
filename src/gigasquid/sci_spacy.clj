(ns gigasquid.sci-spacy
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [clojure.java.shell :as sh]))

;;;; You need to pip install the model
;; sudo pip3 install https://s3-us-west-2.amazonaws.com/ai2-s2-scispacy/releases/v0.2.4/en_core_sci_sm-0.2.4.tar.gz

(require-python '[spacy :as spacy])
(require-python '[scispacy :as scispacy])

(def nlp (spacy/load "en_core_sci_sm"))
(def text "Myeloid derived suppressor cells (MDSC) are immature 
  myeloid cells with immunosuppressive activity. 
  They accumulate in tumor-bearing mice and humans 
  with different types of cancer, including hepatocellular 
  carcinoma (HCC).")

(def doc (nlp text))

(py/python-type (py.- doc sents)) ;=> :generator
(py/python-type (py.- doc ents)) ;=> :tuple

;;; basically you should map over these things

(map (fn [ent] (py.- ent text)) (py.- doc ents))
;=> ("Myeloid" "suppressor cells" "MDSC" "immature" "myeloid cells" "immunosuppressive activity" "accumulate" "tumor-bearing mice" "humans" "cancer" "hepatocellular \n  carcinoma" "HCC")

;;; what sort of things can you look at on the ent?
(-> (py.- doc ents) first py/att-type-map) ;;; lots!

;; {"_" :underscore,
;;  "__class__" :type,
;;  "__delattr__" :method-wrapper,
;;  "__dir__" :builtin-function-or-method,
;;  "__doc__" :str,
;;  "__eq__" :method-wrapper,
;;  "__format__" :builtin-function-or-method,
;;  "__ge__" :method-wrapper,
;;  "__getattribute__" :method-wrapper,
;;  "__getitem__" :method-wrapper,
;;  "__gt__" :method-wrapper,
;;  "__hash__" :method-wrapper,
;;  "__init__" :method-wrapper,
;;  "__init_subclass__" :builtin-function-or-method,
;;  "__iter__" :method-wrapper,
;;  "__le__" :method-wrapper,
;;  "__len__" :method-wrapper,
;;  "__lt__" :method-wrapper,
;;  "__ne__" :method-wrapper,
;;  "__new__" :builtin-function-or-method,
;;  "__pyx_vtable__" :py-capsule,
;;  "__reduce__" :builtin-function-or-method,
;;  "__reduce_ex__" :builtin-function-or-method,
;;  "__repr__" :method-wrapper,
;;  "__setattr__" :method-wrapper,
;;  "__sizeof__" :builtin-function-or-method,
;;  "__str__" :method-wrapper,
;;  "__subclasshook__" :builtin-function-or-method,
;;  "_fix_dep_copy" :builtin-function-or-method,
;;  "_recalculate_indices" :builtin-function-or-method,
;;  "_vector" :none-type,
;;  "_vector_norm" :none-type,
;;  "as_doc" :builtin-function-or-method,
;;  "conjuncts" :tuple,
;;  "doc" :doc,
;;  "end" :int,
;;  "end_char" :int,
;;  "ent_id" :int,
;;  "ent_id_" :str,
;;  "ents" :list,
;;  "get_extension" :builtin-function-or-method,
;;  "get_lca_matrix" :builtin-function-or-method,
;;  "has_extension" :builtin-function-or-method,
;;  "has_vector" :bool,
;;  "kb_id" :int,
;;  "kb_id_" :str,
;;  "label" :int,
;;  "label_" :str,
;;  "lefts" :generator,
;;  "lemma_" :str,
;;  "lower_" :str,
;;  "merge" :builtin-function-or-method,
;;  "n_lefts" :int,
;;  "n_rights" :int,
;;  "noun_chunks" :generator,
;;  "orth_" :str,
;;  "remove_extension" :builtin-function-or-method,
;;  "rights" :generator,
;;  "root" :token,
;;  "sent" :span,
;;  "sentiment" :float,
;;  "set_extension" :builtin-function-or-method,
;;  "similarity" :builtin-function-or-method,
;;  "start" :int,
;;  "start_char" :int,
;;  "string" :str,
;;  "subtree" :generator,
;;  "tensor" :ndarray,
;;  "text" :str,
;;  "text_with_ws" :str,
;;  "to_array" :builtin-function-or-method,
;;  "upper_" :str,
;;  "vector" :ndarray,
;;  "vector_norm" :float-32,
;;  "vocab" :vocab}

;;; same with sentences
(map (fn [sent] (py.- sent text)) (py.- doc sents))
;; ("Myeloid derived suppressor cells (MDSC) are immature \n  myeloid cells with immunosuppressive activity. \n  "
;;  "They accumulate in tumor-bearing mice and humans \n  with different types of cancer, including hepatocellular \n  carcinoma (HCC).")


(require-python '[spacy.displacy :as displacy])
(spit "my-pic.svg" (displacy/render (first (py.- doc sents)) :style "dep"))
(sh/sh "open" "-a" "Google Chrome" "my-pic.svg")

