(ns gigasquid.nltk
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]))

;;; What is NLTK ?
;;; https://www.nltk.org/
;; ;; NLTK is a leading platform for building Python programs to work with human language data.
;; It provides easy-to-use interfaces to over 50 corpora and lexical resources such as WordNet,
;; along with a suite of text processing libraries for classification, tokenization, stemming,
;; tagging, parsing, and semantic reasoning, wrappers for industrial-strength NLP libraries
;; and an active discussion forum.

(require-python '[nltk :as nltk])
(comment 

  ;; We will follow some examples from here first
 ;; https://www.nltk.org/book/ch01.html

;;; you can download individual packages using a parameter
  ;(nltk/download "wordnet")
;;; you can install just what you need for the examples
  ;(nltk/download "book")
;;;; install just the corpora, no grammars or trained models using
  ;(nltk/download "all-corpora")
;;;; or a popular subset
  ;(nltk/download "popular")
;;; or you can download everything with "all"
  ;(nltk/download "all") ;;; BEWARE it downloads lots!

  ;;;; Book datasets
  (nltk/download "book")
  (require-python '[nltk.book :as book])

  (book/texts)
  ;;; prints out in repl
  ;; text1: Moby Dick by Herman Melville 1851
  ;; text2: Sense and Sensibility by Jane Austen 1811
  ;; text3: The Book of Genesis
  ;; text4: Inaugural Address Corpus
  ;; text5: Chat Corpus
  ;; text6: Monty Python and the Holy Grail
  ;; text7: Wall Street Journal
  ;; text8: Personals Corpus
  ;; text9: The Man Who Was Thursday by G . K . Chesterton 1908

  book/text1 ;=>  <Text: Moby Dick by Herman Melville 1851>
  book/text2 ;=>  <Text: Sense and Sensibility by Jane Austen 1811>

  ;;; concordance veiw of a givin word gives every occurence

  (py. book/text1 concordance "monstrous")
  ;;; displays in repl
  ;Displaying 11 of 11 matches:
  ;; ong the former , one was of a most monstrous size . ... This came towards us , 
  ;; ON OF THE PSALMS . " Touching that monstrous bulk of the whale or ork we have r
  ;; ll over with a heathenish array of monstrous clubs and spears . Some were thick
  ;; d as you gazed , and wondered what monstrous cannibal and savage could ever hav
  ;; that has survived the flood ; most monstrous and most mountainous ! That Himmal
  ;; they might scout at Moby Dick as a monstrous fable , or still worse and more de
  ;; th of Radney .'" CHAPTER 55 Of the Monstrous Pictures of Whales . I shall ere l
  ;; ing Scenes . In connexion with the monstrous pictures of whales , I am strongly
  ;; ere to enter upon those still more monstrous stories of them which are to be fo
  ;; ght have been rummaged out of this monstrous cabinet there is no telling . But 
  ;; of Whale - Bones ; for Whales of a monstrous size are oftentimes cast up dead u



  ;;; What other words appear in a similar range of contexts
  (py. book/text1 similar "monstrous")
  ;;; displays in repl
  ;; contemptible christian abundant few part mean careful puzzled
  ;; mystifying passing curious loving wise doleful gamesome singular
  ;; delightfully perilous fearless

  (py. book/text2 similar "monstrous")
  ;; displays in repl
   ;; delightfully perilous fearless
   ;; very so exceedingly heartily a as good great extremely remarkably
   ;; sweet vast amazingly

  ;;; see what sort of methods that this "Text" object has
  (py/att-type-map book/text3)
  ;;; get the length of the book of Genesis
  (py/len book/text3) ;=> 44764
  ;; or get the tokens and count them in clojure
  (count (py.- book/text3 tokens))  ;=> 44764

  ;;; get the sorted set of tokens
  (-> (py.- book/text3 tokens) set count) ;=> 2789

  ;;; lexical diversity (measure of the richness of text )
  (defn lexical-diversity [text]
    (let [tokens (py.- text tokens)]
      (/ (-> tokens set count)
         (* 1.0 (count tokens)))))

  (lexical-diversity book/text3) ;=> 0.06230453042623537
  (lexical-diversity book/text5) ;=> 0.13477005109975562


  ;;; Moving onto Chapter 2 https://www.nltk.org/book/ch02.html

  ;;; Accessing Text Corpora

  (require-python '[nltk.corpus :as corpus])

  ;; NLTK includes a small selection of texts from the Project Gutenberg electronic text archive, which contains some 25,000 free electronic books, hosted at http://www.gutenberg.org/. We begin by getting the Python interpreter to load the NLTK package, then ask to see nltk.corpus.gutenberg.fileids(), the file identifiers in this corpus:
  
  (py. corpus/gutenberg fileids)
                                        ;=> ['austen-emma.txt', 'austen-persuasion.txt', 'austen-sense.txt', 'bible-kjv.txt', 'blake-poems.txt', 'bryant-stories.txt', 'burgess-busterbrown.txt', 'carroll-alice.txt', 'chesterton-ball.txt', 'chesterton-brown.txt', 'chesterton-thursday.txt', 'edgeworth-parents.txt', 'melville-moby_dick.txt', 'milton-paradise.txt', 'shakespeare-caesar.txt', 'shakespeare-hamlet.txt', 'shakespeare-macbeth.txt', 'whitman-leaves.txt']

 ;;; let's pick out emma
  (def emma (py. corpus/gutenberg words "austen-emma.txt"))
  (py/len emma) ;=>192427

;;;;;; Switching over to another tutorial
  ;; https://www.datacamp.com/community/tutorials/text-analytics-beginners-nltk


;;; Sentence tokenization
  (require-python '[nltk.tokenize :as tokenize])

 (def text "Hello Mr. Smith, how are you doing today? The weather is great, and city is awesome.
The sky is pinkish-blue. You shouldn't eat cardboard")
 (def tokenized-sent (tokenize/sent_tokenize text))
 tokenized-sent
 ;;=> ['Hello Mr. Smith, how are you doing today?', 'The weather is great, and city is awesome.', 'The sky is pinkish-blue.', "You shouldn't eat cardboard"]


 (def tokenized-word (tokenize/word_tokenize text))
 tokenized-word
  ;;=> ['Hello', 'Mr.', 'Smith', ',', 'how', 'are', 'you', 'doing', 'today', '?', 'The', 'weather', 'is', 'great', ',', 'and', 'city', 'is', 'awesome', '.', 'The', 'sky', 'is', 'pinkish-blue', '.', 'You', 'should', "n't", 'eat', 'cardboard']
 
 ;;; Frequency Distribution

 (require-python '[nltk.probability :as probability])

 (def fdist (probability/FreqDist tokenized-word))
 fdist ;=> <FreqDist with 25 samples and 30 outcomes>

 (py. fdist most_common)
                                        ;=> [('is', 3), (',', 2), ('The', 2), ('.', 2), ('Hello', 1), ('Mr.', 1), ('Smith', 1), ('how', 1), ('are', 1), ('you', 1), ('doing', 1), ('today', 1), ('?', 1), ('weather', 1), ('great', 1), ('and', 1), ('city', 1), ('awesome', 1), ('sky', 1), ('pinkish-blue', 1), ('You', 1), ('should', 1), ("n't", 1), ('eat', 1), ('cardboard', 1)]


;;; stopwords (considered noise in tett)

 (require-python '[nltk.corpus :as corpus])

 (def stop-words (into #{} (py. corpus/stopwords words "english")))
 stop-words
                                        ;=> #{"d" "itself" "more" "didn't" "ain" "won" "hers" "ours" "further" "shouldn" "his" "him" "hasn't" "s" "doesn" "are" "didn" "don't" "very" "you'd" "under" "who" "which" "isn" "of" "this" "after" "once" "up" "off" "she" "shan't" "nor" "does" "theirs" "ll" "yours" "not" "mustn't" "it" "over" "by" "she's" "it's" "hasn" "is" "few" "shouldn't" "why" "doing" "mightn't" "about" "they" "you" "its" "than" "those" "where" "just" "for" "needn" "should" "my" "again" "themselves" "should've" "ourselves" "whom" "yourselves" "because" "any" "most" "you've" "mustn" "you're" "can" "were" "weren" "ma" "did" "was" "that" "mightn" "if" "same" "both" "doesn't" "don" "had" "what" "an" "or" "have" "couldn't" "am" "couldn" "won't" "their" "a" "so" "them" "weren't" "wouldn" "on" "shan" "own" "above" "but" "when" "until" "be" "haven" "t" "having" "out" "aren't" "that'll" "herself" "and" "do" "myself" "i" "down" "hadn" "here" "too" "y" "between" "such" "needn't" "against" "each" "how" "other" "from" "these" "while" "no" "with" "now" "some" "will" "himself" "all" "you'll" "wouldn't" "re" "then" "isn't" "through" "yourself" "has" "haven't" "being" "our" "during" "wasn" "ve" "before" "only" "your" "to" "into" "m" "aren" "we" "as" "wasn't" "he" "me" "at" "below" "o" "the" "her" "been" "there" "in" "hadn't"}

 ;;; removing stopwords

 (def filtered-sent (->> tokenized-sent
                         (map tokenize/word_tokenize)
                         (map #(remove stop-words %))))
 filtered-sent
 ;; (("Hello" "Mr." "Smith" "," "today" "?")
 ;; ("The" "weather" "great" "," "city" "awesome" ".")
 ;; ("The" "sky" "pinkish-blue" ".")
 ;; ("You" "n't" "eat" "cardboard"))


 ;;;; Lexicon Normalization
 ;;stemming

 (require-python '[nltk.stem :as stem])

 (let [ps (stem/PorterStemmer)]
   (->> filtered-sent
        (map (fn [sent] (map #(py. ps stem %) sent)))))
 ;;=> (("hello" "mr." "smith" "," "today" "?")
 ;;   ("the" "weather" "great" "," "citi" "awesom" ".")
 ;;   ("the" "sky" "pinkish-blu" ".") ("you" "n't" "eat" "cardboard")
 

;;; Lemmatization

 (require-python '[nltk.stem.wordnet :as wordnet])

 (let [lem (wordnet/WordNetLemmatizer)
       stem (stem/PorterStemmer)
       word "flying"]
   {:lemmatized-word (py. lem lemmatize word "v")
    :stemmed-word (py. stem stem word)})
                                        ;=> {:lemmatized-word "fly", :stemmed-word "fli"}

;;; POS Tagging
 (let [sent "Albert Einstein was born in Ulm, Germany in 1879."
       tokens (nltk/word_tokenize sent)]
   {:tokens tokens
    :pos-tag (nltk/pos_tag tokens)})
 ;; {:tokens
 ;; ['Albert', 'Einstein', 'was', 'born', 'in', 'Ulm', ',', 'Germany', 'in', '1879', '.'],
 ;; :pos-tag
 ;; [('Albert', 'NNP'), ('Einstein', 'NNP'), ('was', 'VBD'), ('born', 'VBN'), ('in', 'IN'), ('Ulm', 'NNP'), (',', ','), ('Germany', 'NNP'), ('in', 'IN'), ('1879', 'CD'), ('.', '.')]}




 )









