(ns stand-lib.re-frame.components
  (:require
   [cljs.reader :as reader]
   [reagent.core :as r :refer [atom]]
   [re-frame.core :as rf]
   [stand-lib.reframe.utils :refer
    [extract-ns+name make-keys set-state update-state]]))

; ------------------------------------------------------------------------------
; Subs
; ------------------------------------------------------------------------------

(defn query-sub [db [event ns+name]]
  (get-in db (extract-ns+name ns+name)))

(rf/reg-sub :query query-sub)

; ------------------------------------------------------------------------------
; Handlers

; The following handlers are required:
; #{:set-state, :update-state}
; ------------------------------------------------------------------------------

; ------------------------------------------------------------------------------
; Debugging
; ------------------------------------------------------------------------------

(defn pretty-display [data]
  [:pre
   (with-out-str
    (cljs.pprint/pprint @data))])

; ------------------------------------------------------------------------------
; Forms
; ------------------------------------------------------------------------------

; Helpers ----------------------------------------------------------------------

(defn set-state-callback [ns+name]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (set-state ks (-> comp .-target .-value)))))

(defn set-state-with-value-callback [ns+name val]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (set-state ks val))))

(defn set-state-with-reader-callback [ns+name]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (set-state ks (-> comp .-target .-value reader/read-string)))))

(defn update-state-callback [ns+name f]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (update-state ks f))))

; Core -------------------------------------------------------------------------

(defn form-group [label & input]
  [:div.form-group
   [:label label]
   (into
    [:div]
    input)])

(defmulti input
  "Input component for `:type`s -> :checkbox, :radio, :number, and :text"
  (fn [attrs] (:type attrs)))

(defmethod input :text
  [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback (:name attrs))))]
    [:input edited-attrs]))

; Use of `cljs.reader/read-string` to coerce the e.target.value to number.
; I'm sure there's a better way of doing this, but I haven't figured it out yet.
(defmethod input :number
  [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-with-reader-callback (:name attrs))))]
    [:input edited-attrs]))

; By default the checkbox state is designed to be stored in a single set.
; By default we figure out if the checkbox is checked based on its value's
; presence in that set.
; To override this behavior one must roll their own :on-change and :checked
; attributes.
(defmethod input :checkbox
  [attrs]
  (let [acc (rf/subscribe [:query (:name attrs)])
        f (fn [acc]
            (let [val (:value attrs)]
              (cond
                (nil? acc) #{val}
                (contains? acc val) (disj acc val)
                :default (conj acc val))))
        edited-attrs
        (-> attrs
            (update :on-change #(or % (update-state-callback (:name attrs) f)))
            (update :checked
                    #(or %
                         (when (contains? @acc (:value attrs))
                           true))))]
    [:input edited-attrs]))

; The :value attribute is used so we don't need to bother coercing the
; e.target.value to its original type.
(defmethod input :radio
  [attrs]
  (let [edited-attrs (assoc attrs :on-change #(or % (set-state-with-value-callback (:name attrs) (:value attrs))))]
    [:input edited-attrs]))

(defmethod input :date
  [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback (:name attrs))))]
    [:input edited-attrs]))

(defmethod input :file
  [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback (:name attrs))))]
    [:input edited-attrs]))

(defn textarea [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback (:name attrs))))]
    [:textarea edited-attrs]))

; With the current implementation I dont' think it does not support options
; with strings because `cljs.reader/read-string` returns them as symbols.
(defn select [attrs & options]
  (let [ks (make-keys (:name attrs))
        ;; get the :value of this first option component
        default-val (-> options ffirst second :value)
        edited-attrs
        (-> attrs
            (update :on-change #(or % (set-state-with-reader-callback (:name attrs))))
            ;; If the select has a default value we must persist it, otherwise
            ;; we set it to the first option's value.
            (update :value
                    #(or (and % (do (set-state ks %)
                                    %))
                         (do (set-state ks default-val)
                             default-val))))]
    (into
     [:select edited-attrs]
     options)))

; ------------------------------------------------------------------------------
; MISC
; ------------------------------------------------------------------------------

(defn breadcrumbs [& items]
  (into
   [:ol.breadcrumb
    [:li [:a {:href "/"} "Home"]]]
   (for [{:keys [href title active?] :as item} items]
     (if active?
       [:li.active title]
       [:li [:a {:href href} title]]))))

(defn base [& body]
  (into
   [:div.container]
   body))

(defn thead [headers]
  [:thead
   [:tr
    (for [th headers]
      ^{:key th}
      [:th.text-center th])]])

(defn tbody [rows]
  (into
   [:tbody]
   (for [row rows]
     (into
      [:tr]
      (for [td row]
        [:td.text-center td])))))

(defn thead-indexed
  "Coupled with `tbody-indexed`, allocates a col for the row's index."
  [headers]
  [:thead
   (into
     [:tr
      [:th.text-center "#"]]
     (for [th headers]
       [:th.text-center th]))])

(defn tbody-indexed
  "Coupled with `thead-indexed`, allocates a col for the row's index."
  [rows]
  (into
   [:tbody]
   (map-indexed
    (fn [i row]
      (into
       [:tr [:td.text-center (inc i)]]
       (for [td row]
         [:td.text-center
          td])))
    rows)))

(defn thead-editable [headers]
  [thead
   (conj headers "Edit" "Delete")])
