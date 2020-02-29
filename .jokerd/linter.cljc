(ns libpython-clj.python)

(defmacro with [& form]
  `(let ~@form))

(defmacro py. [& form]
  (let [[member-symbol instance-member & args] form]
    `(str (pr ~member-symbol)
          (prn ~@args)
          (pr nil))))

(defmacro py.- [& form]
  (let [[member-symbol instance-field] form]
    `(str (pr ~member-symbol)
          (pr nil))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns libpython-clj.require)

(defmacro require-python [form]
  (let [form (second form) ;; first is (quote ...)
        [_ & {:keys [as refer]}] form]
    (let [references (if refer [:refer refer] '())]
      (apply list
             (cond-> `[do]
               as (conj `(create-ns (quote ~as))
                        `(require (quote [~as ~@references]))))))))
(comment
  (*require-python '[torch.optim.lr_scheduler :as lr_scheduler]))
