(ns stand-lib.local-store
  (:require
   [re-frame.core :as rf]))

(declare save)

; ------------------------------------------------------------------------------
; re-frame utils
; ------------------------------------------------------------------------------

;; This interceptor stores todos into local storage.
;; We attach it to each event handler which could update todos
(def ->local-store (rf/after save))

; ------------------------------------------------------------------------------
; Core
; ------------------------------------------------------------------------------

(defn clear!
  [ls-key]
  (.setItem js/localStorage ls-key ""))

(defn save!
  "Puts colls into localStorage as EDN"
  [ls-key colls]
  (.setItem js/localStorage ls-key
            (str colls))
  colls)

(defn load
  "Read in the EDN data structure from localstore."
  [ls-key]
  (some->> (.getItem js/localStorage ls-key)
           (cljs.reader/read-string)))


; ------------------------------------------------------------------------------
; Utils
; ------------------------------------------------------------------------------

(defn allocate-next-id
  "Returns the next item id.
  Assumes items are a coll of maps with an `:id` field, ordered by this field
  Returns one more than the current largest id."
  [colls idfield]
  ((fnil inc 0) (get (first colls) idfield)))

(defn insert!
  "Takes a map with keys `into` `id` and `keyvals`.
  The first is the localStorage key, the second the id key that will be auto-
  matically incremented, and the last is the key-values being created."
  [{:keys [into id keyvals]}]
  (let [colls (load into)
        keyvals+id (assoc keyvals
                          id (allocate-next-id colls id))]
    (save! into
           ((fnil conj []) colls keyvals+id))
    keyvals+id))

(defn update!
  "Takes a localStorage key and a map with keys `where` and `set`.
  `where` is the predicate used to identify which colls to update.
  `set` is a map that will be merged upon the original map(s) returned
  by the `where` predicate."
  [ls-key {:keys [where set]}]
  (letfn [(edit-where [colls pred new-m]
            (map (fn [m]
                   (if (pred m)
                     (merge m new-m)
                     m))
                 colls))]
    (-> (load ls-key)
        (edit-where where set)
        (as-> colls
              (save! ls-key colls)))))

(defn delete!
  "Takes a map with keys `from` and `where`.
  The first one is the key to localStorage, and the second a predicate. We
  remove items that return true for `(where item)`."
  [{:keys [from where]}]
  (->> (load from)
       (remove where)
       (save! from)))


(defn select
  "Takes a map with two keys: `from` and `where`. The former is the coll
  where selecting, the latter is a test function. If `(where item)` is true
  we return `item`."
  [{:keys [from where]}]
  (cond
    (coll? from) (filter where from)
    ;; if `from` is the ls-key we load it
    ;; if where is not given we return everything
    (nil? where)
    (load from)
    ;; otherwise we filter it
    :else (->> (load from)
               (filter where))))
