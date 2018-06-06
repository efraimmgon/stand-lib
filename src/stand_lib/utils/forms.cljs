(ns stand-lib.utils.forms
  (:require
   [clojure.string :as string]
   [re-frame.core :as rf]
   [stand-lib.utils :refer [num-or-str]]))

; It seems the best course of action is to stop fighting your host language
; at every corner. Instead, we'll work with it the best we can and
; simply coerce types at the fringes and let the core work with js's
; inconsistencies.

; What does this means? We will store everything as strings
; and the coercions will be done by the user, at the point he thinks best.

; This will suck for things like checkbox and select inputs, I suppose. Will
; it make it unfeasable or unsuferable, though? Time will tell.

; - Radio and select inputs can have a single value mapped to their name attr.
; - Checkbox inputs can have one or more values mapped to its name attr.

; ------------------------------------------------------------------------------
; Helpers' helpers
; ------------------------------------------------------------------------------

(defn target-value [event]
  (.-value (.-target event)))

(defn set-state! [path v]
  (rf/dispatch [:set-state path v])
  v)

; ------------------------------------------------------------------------------
; Core
; ------------------------------------------------------------------------------

(defn handle-change-at! [path e]
  (set-state! path (target-value e)))

(defn set-value-at! [path e]
  (rf/dispatch [:set-state path (target-value e)]))

(defn toggle-value! [path]
  (let [update-fn not]
    (rf/dispatch [:update-state path update-fn])))

(defn handle-mopt-change-at
  "For checkboxes, for instance, maps `path` to a set, handling insertion and
  removal of values as per user interaction."
  [path val]
  (let [update-f (fn [a-set]
                   (cond
                     (nil? a-set) #{val}
                     (contains? a-set val) (disj a-set val)
                     :else (conj a-set val)))]
    (rf/dispatch [:update-state path update-f])))
