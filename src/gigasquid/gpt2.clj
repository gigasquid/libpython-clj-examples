(ns gigasquid.gpt2
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py]))

;;; sudo pip3 install torch
;;; sudo pip3 install transformers

;https://huggingface.co/transformers/quickstart.html - OpenAI GPT-2

(require-python '(transformers))
(require-python '(torch))


;;; Load pre-trained model tokenizer (vocabulary)

(def tokenizer (py/$a transformers/GPT2Tokenizer from_pretrained "gpt2"))
(def text "Who was Jim Henson ? Jim Henson was a")
;; encode text input
(def indexed-tokens  (py/$a tokenizer encode text))
indexed-tokens ;=>[8241, 373, 5395, 367, 19069, 5633, 5395, 367, 19069, 373, 257]

;; convert indexed tokens to pytorch tensor
(def tokens-tensor (torch/tensor [indexed-tokens]))
tokens-tensor
;; ([[ 8241,   373,  5395,   367, 19069,  5633,  5395,   367, 19069,   373,
;;    257]])

;;; Load pre-trained model (weights)
;;; Note: this will take a few minutes to download everything
(def model (py/$a transformers/GPT2LMHeadModel from_pretrained "gpt2"))

;;; Set the model in evaluation mode to deactivate the DropOut modules
;;; This is IMPORTANT to have reproducible results during evaluation!
(py/$a model eval)


;;; Predict all tokens
(def predictions (py/with [r (torch/no_grad)]
                          (first (model tokens-tensor))))

;;; get the predicted next sub-word"
(def predicted-index (let [last-word-predictions (->  predictions first last)
                           arg-max (torch/argmax last-word-predictions)]
                       (py/$a arg-max item)))

predicted-index ;=>582

(py/$a tokenizer decode (-> (into [] indexed-tokens)
                            (conj predicted-index)))

;=> "Who was Jim Henson? Jim Henson was a man"


;;;; ===========================

;; GPT-2 as well as some other models (GPT, XLNet, Transfo-XL, CTRL) make use of a past or mems attribute which can be used to prevent re-computing the key/value pairs when using sequential decoding. It is useful when generating sequences as a big part of the attention mechanism benefits from previous computations.

;; Here is a fully-working example using the past with GPT2LMHeadModel and argmax decoding (which should only be used as an example, as argmax decoding introduces a lot of repetition):

(def tokenizer (py/$a transformers/GPT2Tokenizer from_pretrained "gpt2"))
(def model (py/$a transformers/GPT2LMHeadModel from_pretrained "gpt2"))

(def generated (into [] (py/$a tokenizer encode "The Manhattan bridge")))
(def context (torch/tensor [generated]))


(defn generate-sequence-step [{:keys [generated-tokens context past]}]
  (let [[output past] (model context :past past)
        token (torch/argmax (first output))
        new-generated  (conj generated-tokens (py/$a token tolist))]
    {:generated-tokens new-generated
     :context (py/$a token unsqueeze 0)
     :past past
     :token token}))

(defn decode-sequence [{:keys [generated-tokens]}]
  (py/$a tokenizer decode generated-tokens))

(loop [step {:generated-tokens generated
             :context context
             :past nil}
       i 10]
  (if (pos? i)
    (recur (generate-sequence-step step) (dec i))
    (decode-sequence step)))

;=> "The Manhattan bridge\n\nThe Manhattan bridge is a major artery for"


;;; Let's make a nice function to generate text

(defn generate-text [starting-text num-of-words-to-predict]
  (let [tokens (into [] (py/$a tokenizer encode starting-text))
        context (torch/tensor [tokens])
        result (reduce
                (fn [r i]
                  (println i)
                  (generate-sequence-step r))

                {:generated-tokens tokens
                 :context context
                 :past nil}

                (range num-of-words-to-predict))]
    (decode-sequence result)))

(generate-text "Natural language processing tasks are typically approached with" 
               100)

;=> "Clojure is a dynamic, general purpose programming language, combining the approachability and interactive. It is a language that is easy to learn and use, and is easy to use for anyone"



;;;;;; Better sequence generating
;;; With temperature to get rid of repititions

;;; from https://github.com/huggingface/transformers/issues/1725

(require-python '(torch.nn.functional))

(defn sample-sequence-step [{:keys [generated-tokens context past temp]
                             :or {temp 0.8}}]
  (let [[output past] (py/with [r (torch/no_grad)]
                       (model context :past past))
        next-token-logits (torch/div (-> output first last)
                                     (if (pos? temp) temp 1))
        token (torch/multinomial
               (torch.nn.functional/softmax next-token-logits :dim -1) :num_samples 1)
        new-generated  (conj generated-tokens (first (py/$a token tolist)))]
    {:generated-tokens new-generated
     :context (py/$a token unsqueeze 0)
     :past past
     :token token}))

(defn generate-text2 [starting-text num-of-words-to-predict temp]
  (let [tokens (into [] (py/$a tokenizer encode starting-text))
        context (torch/tensor [tokens])
        result (reduce
                (fn [r i]
                  (println i)
                  (sample-sequence-step (assoc r :temp temp)))

                {:generated-tokens tokens
                 :context context
                 :past nil}

                (range num-of-words-to-predict))]
    (decode-sequence result)))

(generate-text2 "Natural language processing tasks are typically approached with" 
                100
                0.8)

;>"Natural language processing tasks are typically approached with distress signals and pleasurable stimuli.\n\n7.2.3. Structural networks\n\nStructural networks are comprised of various layers of information that are coupled with instructions for performing behavioral tasks. Such networks can be used for e.g., associating individual groups with special differential activities (e.g., listening to music, studying a subject's handwriting), or for performing complex tasks such as reading and writing a chart. The presence of structures that are familiar to the participant may also help"

(generate-text2 "It is thought that cheese was first discovered around 8000 BC around the time when sheep were first domesticated"
                100
                0.8)
;=>"It is thought that cheese was first discovered around 8000 BC around the time when sheep were first domesticated as sheep. Native American plants and animals associated with such plants are described as being \"mushy, leafy and musky\" from having \"powder-like stalks and narrow niche-like leaves.\" They are believed to have been found in the Cauca Chaco area of South America and northern Mexico. The earliest known cases of cheese in the Americas could be traced back to around 160 BC, when the deposits of the Cauca Chaco were discovered in Colombia, Peru and Argentina"


(generate-text2 "Rich Hickey developed Clojure because he wanted a modern Lisp for functional programming, symbiotic with the established Java platform"
                100
                0.8)
"Rich Hickey developed Clojure because he wanted a modern Lisp for functional programming, symbiotic with the established Java platform. He knew that Clojure would make it hard to access any memory through Java, and code a good amount of Lisp. He had much to learn about programming at the time, and Clojure was perfect for him. It was important to understand the dominant language of Lisp, which was Clojure and JVM. Because of this, JVM was named 'Snack: No Slobs in Clojure'. This was a very important order of things, for JVM. Clojure had a major advantage over JVM in"

