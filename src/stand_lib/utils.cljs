(ns stand-lib.utils
  (:require
   [clojure.string :as string]))

; ------------------------------------------------------------------------------
; MISC
; ------------------------------------------------------------------------------

(defn domap
  "Implementation of Common Lisp `mapc`. It is like `map` except that the
   results of applying function are not accumulated. The `colls` argument
   is returned."
  [f & colls]
  (reduce (fn [_ args]
            (apply f args))
          nil (apply map list colls))
  colls)

(defn deep-merge-with [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))

(defn- keyword-or-int [x]
  (if (int? (js/parseInt x))
    (js/parseInt x)
    (keyword x)))

(defn extract-ns+name [k]
  (mapv keyword-or-int
        (if (qualified-keyword? k)
          (into (string/split (namespace k) ".")
                (string/split (name k) "."))
          (string/split (name k) "."))))

(defn make-keys [x]
  (if (vector? x)
    x
    (extract-ns+name x)))
