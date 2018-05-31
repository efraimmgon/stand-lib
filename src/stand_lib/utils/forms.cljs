(ns stand-lib.utils.forms
  (:require
   [clojure.string :as string]
   [re-frame.core :as rf]
   [stand-lib.utils :refer [num-or-str]]))

; It seems the best course of action is to stop fighting your host language
; at every corner. Instead, well work with it the best we can and we'll
; simply coerce things to our way at the fringes and let the core work with
; the same inconsistencies as the host.

; What this means? The core form components will store everything as a string
; and the coercions will be done by the user, at the point he thinks best.

; This will suck for things like checkbox and select inputs, I suppose. Will
; it make it unfeasable or unsuferable, though?

; - Radio and select inputs can have a single value mapped to their name attr.
; - Checkbox inputs can have one or more values mapped to its name attr.

(defn target-value [event]
  (.-value (.-target event)))

(defn set-state! [path v]
  (rf/dispatch [:set-state path v])
  v)

(defn handle-change-at [path e]
  (set-state! path (target-value e)))

(defn handle-mopt-change-at
  "For checkboxes, for instance, maps `path` to a set, handling insertion and
  removal of values as per user interaction."
  [path e]
  (let [val (target-value e)
        update-f (fn [a-set]
                   (cond
                     (nil? a-set) #{val}
                     (contains? a-set val) (disj a-set val)
                     :else (conj a-set val)))]
    (rf/dispatch [:update-state path update-f])))
