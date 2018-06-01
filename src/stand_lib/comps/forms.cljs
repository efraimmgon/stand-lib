(ns stand-lib.comps.forms
  (:require
   [re-frame.core :as rf]
   [stand-lib.utils.forms :refer
    [handle-change-at handle-mopt-change-at set-state!]]))

; Although different input types do different things, the core of the
; implementation revolves around the `on-change` fn.
; Given my personal belive that re-frame is essential for SPA development
; using reagent, our implementation relies heavily on it.

; With the intention of allowing users to customize the components to their
; uses, they are able to provide custom values to some methods.
; In all cases, they can provide a custom `on-change` fn. Other custom values
; can be provided, as it makes sense for each input type.

; Given our implementation choice, the `:name` field must refer to a
; `re_frame.db.app_db.state` location.
; It can be given as a keyword, `:a.db.location` or  `:a.db/location`,
; or a vector of keywords, `[:a :db :location]`.
; I.e:
; [input {:name :users.user/name ; or :users.user.name or [:users :user :name]
;         :type :text
;         :required true}]
(defmulti input :type)

; Inputs of type:
; - `text`
; - `number`
; refer to it.
; Required keys: `:name`, `:on-change`.
; Available fields: `:value`.
(defmethod input :default
  [attrs]
  (let [stored-val (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :value #(or % @stored-val)))]
    [:input edited-attrs]))

; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `default-checked`.
; The `checked` field, is reserved for the inner implementation.
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

; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `default-checked`.
; The `checked` field, is reserved for the inner implementation.
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

; Required keys: `:name`, `:on-change`.
; Available fields: `:value`.
(defn textarea [attrs]
  (let [stored-val (rf/subscribe [:query (:name attrs)])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (handle-change-at (:name attrs) e))))
            (update :value #(or % @stored-val)))]
    [:textarea edited-attrs]))

; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `default-value`.
; NOTE: As per React, `:default-value` replaces the `selected` property
; functionality.
; NOTE: Our select allows a single option.
; TODO: Allow multiple options to be selected.
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
