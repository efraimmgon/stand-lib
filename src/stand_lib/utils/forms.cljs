(ns stand-lib.utils.forms
  (:require
   [re-frame.core :as rf]))

(defn get-value [event]
  (-> event .-target .-value))

(defn swap-val-at! [path v]
  (rf/dispatch [:set-state path v])
  v)

(defn handle-change-at [path e]
  (rf/dispatch [:set-state path (get-value e)]))

(defn handle-n-change-at [path e]
  (let [n (js/parseFloat (get-value e))]
    (rf/dispatch [:set-state path (when-not (js/isNaN n) n)])))

(defn handle-opt-change-at [path e]
  (rf/dispatch [:set-state path (keyword-or-int (get-value e))]))

(defn handle-mopt-change-at [path e]
  (let [val (keyword-or-int (get-value e))
        f (fn [acc]
              (cond
                (nil? acc) #{val}
                (contains? acc val) (disj acc val)
                :else (conj acc val)))]
    (rf/dispatch [:update-state path f])))
