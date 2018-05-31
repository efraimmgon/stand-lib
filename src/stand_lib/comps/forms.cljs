(ns stand-lib.comps.forms
  (:require
   [re-frame.core :as rf]
   [stand-lib.utils.forms :refer
    [handle-change-at handle-n-change-at handle-mopt-change-at
     handle-opt-change-at reset-val-at!]]))

(defmulti input :type)

;; Default is of whatever type is given
(defmethod input :default
  [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :value #(or % @field-value)))]
    [:input edited-attrs]))

(defmethod input :number
  [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :type :text)
            (update :on-change #(or % (fn [e] (handle-n-change-at (:name attrs) e))))
            (update :value #(or % @field-value)))]
    [:input edited-attrs]))

(defmethod input :radio
  [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :type :radio)
            (update :on-change #(or % (fn [e] (handle-opt-change-at (:name attrs) e))))
            (update :checked #(or % (or (:default-checked attrs)
                                        (= (:value attrs) @field-value))))
            (dissoc :default-checked))]
    ;; persist value when it's the default
    (when (and (nil? @field-value) (:checked edited-attrs))
      (reset-val-at! (:name edited-attrs) (:value edited-attrs)))
    [:input edited-attrs]))

(defmethod input :checkbox
  [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (assoc :type :checkbox)
            (update :on-change #(or % (fn [e] (handle-mopt-change-at (:name attrs) e))))
            (assoc :checked (or (:default-checked attrs)
                                (contains? @field-value (:value attrs))
                                (= (:value attrs) @field-value)))
            (dissoc :default-checked))]
    ;; persist value when it's the default
    (when (and (:checked edited-attrs)
               (not (contains? @field-value (:value attrs))))
      (rf/dispatch [:update-state (:name attrs) #(conj % (:value attrs))]))
    [:input edited-attrs]))

(defn textarea [attrs]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :value #(or % @field-value)))]
    [:textarea edited-attrs]))

(defn select [attrs options]
  (let [field-value (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-opt-change-at (:name attrs) e))))
            (update :value #(or % (or @field-value (:default-value attrs))))
            (dissoc :default-value))]
    (when (and (nil? @field-value) (:default-value attrs))
      (reset-val-at! (:name attrs) (:default-value attrs)))
    [:select edited-attrs
     options]))
