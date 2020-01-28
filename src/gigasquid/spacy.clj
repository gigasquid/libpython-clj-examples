(ns gigasquid.spacy
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]))

;;;; What is SpaCy?

;;; also natural language toolkit https://spacy.io/usage/spacy-101#whats-spacy
;;; opinionated library and more Object oriented than NLTK. Has word vector support
;;; better performance for tokenization and pos tagging (source https://medium.com/@akankshamalhotra24/introduction-to-libraries-of-nlp-in-python-nltk-vs-spacy-42d7b2f128f2)

;;; Install pip3 install spacy
;;;; python3 -m spacy download en_core_web_sm

(require-python '[spacy :as spacy])


;;; Following this tutorial https://spacy.io/usage/spacy-101#annotat

;;; linguistic annotations


(def nlp (spacy/load "en_core_web_sm"))

(let [doc (nlp "Apple is looking at buying U.K. startup for $1 billion")]
  (map (fn [token]
         [(py.- token text) (py.- token pos_) (py.- token dep_)])
       doc))
;; (["Apple" "PROPN" "nsubj"]
;;  ["is" "AUX" "aux"]
;;  ["looking" "VERB" "ROOT"]
;;  ["at" "ADP" "prep"]
;;  ["buying" "VERB" "pcomp"]
;;  ["U.K." "PROPN" "compound"]
;;  ["startup" "NOUN" "dobj"]
;;  ["for" "ADP" "prep"]
;;  ["$" "SYM" "quantmod"]
;;  ["1" "NUM" "compound"]
;;  ["billion" "NUM" "pobj"])


(let [doc (nlp "Apple is looking at buying U.K. startup for $1 billion")]
  (map (fn [token]
         {:text (py.- token text)
          :lemma (py.- token lemma_)
          :pos (py.- token pos_)
          :tag (py.- token tag_)
          :dep (py.- token dep_)
          :shape (py.- token shape_)
          :alpha (py.- token is_alpha)
          :is_stop (py.- token is_stop)} )
       doc))

;; ({:text "Apple",
;;   :lemma "Apple",
;;   :pos "PROPN",
;;   :tag "NNP",
;;   :dep "nsubj",
;;   :shape "Xxxxx",
;;   :alpha true,
;;   :is_stop false}
;;  {:text "is",
;;   :lemma "be",
;;   :pos "AUX",
;;   :tag "VBZ",
;;   :dep "aux",
;;   :shape "xx",
;;   :alpha true,
;;   :is_stop true}
;;  ...


;;; Named entities

(let [doc (nlp "Apple is looking at buying U.K. startup for $1 billion")]
  (map (fn [ent]
         {:text (py.- ent text)
          :start-char (py.- ent start_char)
          :end-char (py.- ent end_char)
          :label (py.- ent label_)} )
       (py.- doc ents)))

;; ({:text "Apple", :start-char 0, :end-char 5, :label "ORG"}
;;  {:text "U.K.", :start-char 27, :end-char 31, :label "GPE"}
;;  {:text "$1 billion", :start-char 44, :end-char 54, :label "MONEY"})


;;; Word Vectors

;; To make them compact and fast, spaCy’s small models (all packages that end in sm) don’t ship with word vectors, and only include context-sensitive tensors. This means you can still use the similarity() methods to compare documents, spans and tokens – but the result won’t be as good, and individual tokens won’t have any vectors assigned. So in order to use real word vectors, you need to download a larger model:

;;;python -m spacy download en_core_web_md (medium one)

;;; then restart cider to pick up changes

(require-python '[spacy :as spacy])
(def nlp (spacy/load "en_core_web_md"))

(let [tokens (nlp "dog cat banana afskfsd")]
  (map (fn [token]
         {:text (py.- token text)
          :has-vector (py.- token has_vector)
          :vector_norm (py.- token vector_norm)
          :is_oov (py.- token is_oov)} )
       tokens))

;; ({:text "dog",
;;   :has-vector true,
;;   :vector_norm 7.033673286437988,
;;   :is_oov false}
;;  {:text "cat",
;;   :has-vector true,
;;   :vector_norm 6.680818557739258,
;;   :is_oov false}
;;  {:text "banana",
;;   :has-vector true,
;;   :vector_norm 6.700014114379883,
;;   :is_oov false}
;;  {:text "afskfsd", :has-vector false, :vector_norm 0.0, :is_oov true})


;;; finding similarity

(let [tokens (nlp "dog cat banana")]
  (for [token1 tokens
        token2 tokens]
    {:token1 (py.- token1 text)
     :token2 (py.- token2 text)
     :similarity (py. token1 similarity token2)}))

;; ({:token1 "dog", :token2 "dog", :similarity 1.0}
;;  {:token1 "dog", :token2 "cat", :similarity 0.8016854524612427}
;;  {:token1 "dog", :token2 "banana", :similarity 0.2432764321565628}
;;  {:token1 "cat", :token2 "dog", :similarity 0.8016854524612427}
;;  {:token1 "cat", :token2 "cat", :similarity 1.0}
;;  {:token1 "cat", :token2 "banana", :similarity 0.28154364228248596}
;;  {:token1 "banana", :token2 "dog", :similarity 0.2432764321565628}
;;  {:token1 "banana", :token2 "cat", :similarity 0.28154364228248596}
;;  {:token1 "banana", :token2 "banana", :similarity 1.0})

