(ns stand-lib.comps.forms
  (:require
   [re-frame.core :as rf]
   [stand-lib.utils.forms :refer
    [handle-change-at handle-n-change-at handle-mopt-change-at
     handle-opt-change-at swap-val-at!]]))


(defn text-input [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :type :text)
            (assoc :on-change #(handle-change-at (:name attrs) %))
            (assoc :value @field-value))]
    [:input edited-attrs]))

(defn number-input [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :type :text)
            (assoc :on-change #(handle-n-change-at (:name attrs) %))
            (assoc :value @field-value))]
    [:input edited-attrs]))

(defn textarea [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :on-change #(handle-change-at (:name attrs) %))
            (assoc :value @field-value))]
    [:textarea edited-attrs]))

(defn radio-input [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :type :radio)
            (assoc :on-change #(handle-opt-change-at (:name attrs) %))
            (assoc :checked (or (:default-checked attrs)
                                (= (:value attrs) @field-value)))
            (dissoc :default-checked))]
    (when (and (nil? @field-value) (:checked edited-attrs))
      (swap-val-at! (:name edited-attrs) (:value edited-attrs)))
    [:input edited-attrs]))

(defn checkbox-input [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :type :checkbox)
            (assoc :on-change #(handle-mopt-change-at (:name attrs) %))
            (assoc :checked (or (:default-checked attrs)
                                (contains? @field-value (:value attrs))
                                (= (:value attrs) @field-value)))
            (dissoc :default-checked))]
    [:input edited-attrs]))


(defn select-input [attrs options]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :on-change #(handle-opt-change-at (:name attrs) %))
            (assoc :value (or @field-value (:default-value attrs)))
            (dissoc :default-value))]
    (when (and (nil? @field-value) (:default-value attrs))
      (swap-val-at! (:name attrs) (:default-value attrs)))
    [:select edited-attrs
     options]))
