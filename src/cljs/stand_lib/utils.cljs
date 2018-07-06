(ns stand-lib.utils
  (:require
   [cljs.string :as string]
   [dommy.core :as dommy :refer-macros [sel sel1]]))

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

(defn extract-ns+name [k]
  (mapv keyword
        (if (qualified-keyword? k)
          (into (string/split (namespace k) ".")
                (string/split (name k) "."))
          (string/split (name k) "."))))

; --------------------------------------------------------------
; DOM Manipulation
; --------------------------------------------------------------

(defn- set-attrs! [elt opts]
  (reduce (fn [elt- [attr val]]
            (dommy/set-attr! elt- attr val))
          elt opts))

(defn add-style!
  "Append a link tag to body. `opts` must include at least the
   `:href` attribute"
  [opts]
  (let [default-attrs {:rel "stylesheet" :type "text/css"}
        elt (set-attrs! (dommy/create-element :link)
                        (into default-attrs opts))]
    (dommy/append! (sel1 :body) elt)))

(defn add-script!
  "Append a script tag to body. `opts` must include at least the
   `:src` attribute."
  [opts]
  (let [default-attrs {:type "text/javascript"}
        elt (set-attrs! (dommy/create-element :script)
                        (into default-attrs opts))]
    (dommy/append! (sel1 :body) elt)))

(defn remove-elt!
  "Remove a tag from the document by the id attribute."
  [id]
  (dommy/remove! (sel1 id)))
