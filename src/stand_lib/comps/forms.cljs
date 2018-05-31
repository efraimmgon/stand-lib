(ns stand-lib.comps.forms
  (:require
   [re-frame.core :as rf]
   [stand-lib.utils.forms :refer
    [handle-change-at handle-mopt-change-at set-state!]]))

(defmulti input :type)

; If a type that is not implemented is given, simply use it.
; Inputs of type:
; - `:text`
; - `:number`
; refer to it.
(defmethod input :default
  [attrs]
  (let [stored-val (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :value #(or % @stored-val)))]
    [:input edited-attrs]))

(defmethod input :radio
  [attrs]
  (let [stored-val (rf/subscribe [:query (:name attrs)])
        svalue (str (:value attrs))
        edited-attrs
        (-> attrs
            (assoc :type :radio)
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :checked #(or % (or (:default-checked attrs)
                                        (= svalue @stored-val))))
            (dissoc :default-checked))]
    ;; Persist value when it's the default:
    (when (and (nil? @stored-val) (:checked edited-attrs))
      (set-state! (:name edited-attrs) svalue))
    [:input edited-attrs]))

(defmethod input :checkbox
  [attrs]
  ;; Initialize the container:
  (set-state! (:name attrs) #{})
  (let [container (rf/subscribe [:query (:name attrs)])
        svalue (str (:value attrs))
        edited-attrs
        (-> attrs
            (assoc :type :checkbox)
            (update :on-change #(or % (fn [e] (handle-mopt-change-at (:name attrs) e))))
            (assoc :checked (or (:default-checked attrs)
                                (contains? @container svalue)))
            (dissoc :default-checked))]
    ;; Persist value when it's the default:
    (when (and (:checked edited-attrs)
               (not (contains? @container svalue)))
      (rf/dispatch [:update-state (:name attrs) #(conj % svalue)]))
    [:input edited-attrs]))

(defn textarea [attrs]
  (let [stored-val (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :value #(or % @stored-val)))]
    [:textarea edited-attrs]))

(defn select [attrs options]
  (let [stored-val (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :value #(or % (or @stored-val (:default-value attrs))))
            (dissoc :default-value))]
    (when (and (nil? @stored-val) (:default-value attrs))
      (set-state! (:name attrs) (:default-value attrs)))
    [:select edited-attrs
     options]))
