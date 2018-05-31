(ns stand-lib.utils.forms
  (:require
   [re-frame.core :as rf]
   [stand-lib.utils :refer [num-or-str]]))

(defn target-value [event]
  (.-value (.-target event)))

(defn reset-val-at! [path v]
  (rf/dispatch [:set-state path v])
  v)

(defn handle-change-at [path e]
  (rf/dispatch [:set-state path (target-value e)]))

; Currently if I have an input with `42`, and type in `a`, for exemple,
; nil will be dispatched, and the input will be blanked.
; Would it be better if I simply denied the `a` char to being submited?
; We will see.
(defn handle-n-change-at
  ; "Coerces input value to number, using `js/parseFloat`. If the resulting value
  ; passes the test for `js/isNaN`, `nil` is dispatched instead."
  [path e]
  (let [n (js/parseFloat (target-value e))]
    (when-not (js/isNaN n)
      (rf/dispatch [:set-state path n]))))

(defn handle-opt-change-at [path e]
  (rf/dispatch [:set-state path (num-or-str (target-value e))]))

(defn handle-mopt-change-at [path e]
  (let [val (num-or-str (target-value e))
        f (fn [acc]
              (cond
                (nil? acc) #{val}
                (contains? acc val) (disj acc val)
                :else (conj acc val)))]
    (rf/dispatch [:update-state path f])))
