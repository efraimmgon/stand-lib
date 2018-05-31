(ns stand-lib.components
  (:require
   [clojure.string :as string]
   [reagent.core :as r :refer [atom]]
   [re-frame.core :as rf]
   [stand-lib.handlers]
   [cljs.pprint]))

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

; Core -------------------------------------------------------------------------

(defn form-group [label & input]
  [:div.form-group
   [:label label]
   (into
    [:div]
    input)])

;; TODO: update input components
(comment
  (defn form-for
    [obj {:keys [ns fields]}]
    [:div
     (doall
       (for [attrs fields]
         (let [edited-attrs
               (-> attrs
                   (update :value #(or % (get @obj (:id attrs))))
                   (assoc :name (keyword ns (:id attrs)))
                   (dissoc :label :id))]
           ^{:key (:id attrs)}
           [form-group
            (:label attrs)
            (cond
              (= :textarea (:type attrs))
              [textarea (merge (dissoc edited-attrs :type)
                               (select-keys attrs [:rows :cols]))]

              (or (= :radio (:type attrs)) (= :checkbox (:type attrs)))
              (doall
                (for [[label value checked?] (:values attrs)]
                  (let [checked-fn (fn []
                                     (cond
                                       checked? true
                                       (= :radio (:type attrs))  (= value (get @obj (:id attrs)))
                                       :else (contains? (get @obj (:id attrs)) value)))]
                    ^{:key value}
                    [:label.form-check-label
                     [input (assoc edited-attrs
                                   :class "form-check-input"
                                   :value value
                                   :default-checked (checked-fn))]
                     " " label])))

              (= :date (:type attrs))
              [input (assoc edited-attrs :value
                            (when-let [d (get @obj (:id attrs))]
                              (if (string? d)
                                d
                                (-> d .toISOString (.split "T") first))))]

              :else [input edited-attrs])])))]))


; ------------------------------------------------------------------------------
; Modal
; ------------------------------------------------------------------------------

(defn modal [header body & [footer]]
  [:div
   [:div.modal-dialog
    [:div.modal-content
     [:div.modal-header [:h3 header]]
     [:div.modal-body body]
     (when footer
       [:div.modal-footer
        [:div.bootstrap-dialog-footer
         footer]])]]
   [:div.modal-backdrop.fade.in]])

; ------------------------------------------------------------------------------
; MISC
; ------------------------------------------------------------------------------

(defn card [{:keys [title subtitle content footer]}]
  [:div.card
   [:div.card-header
    [:h4.card-title title]
    (when subtitle
      [:p.card-category subtitle])]
   [:div.card-body
    content]
   [:div.card-footer
    footer]])


(defn breadcrumbs [& items]
  (into
   [:ol.breadcrumb
    [:li [:a {:href "/"} "Home"]]]
   (for [{:keys [href title active?] :as item} items]
     (if active?
       [:li.active title]
       [:li [:a {:href href} title]]))))

(defn thead [headers]
  [:thead
   [:tr
    (for [th headers]
      ^{:key th}
      [:th (str th)])]])

(defn tbody [rows]
  (into
   [:tbody]
   (for [row rows]
     (into
      [:tr]
      (for [td row]
        [:td (str td)])))))

(defn thead-indexed
  "Coupled with `tbody-indexed`, allocates a col for the row's index."
  [headers]
  [:thead
   (into
     [:tr
      [:th "#"]]
     (for [th headers]
       [:th th]))])

(defn tbody-indexed
  "Coupled with `thead-indexed`, allocates a col for the row's index."
  [rows]
  (into
   [:tbody]
   (map-indexed
    (fn [i row]
      (into
       [:tr [:td (inc i)]]
       (for [td row]
         [:td
          td])))
    rows)))

(defn tabulate
  "Render data as a table.
  `rows` is expected to be a coll of maps.
  `ks` are the a the set of keys from rows we want displayed.
  `class` is css class to be aplied to the `table` element."
  ([ks rows] (tabulate ks rows {}))
  ([attrs ks rows]
   [:table
    attrs
    ;; if there are extra headers we append them
    [thead (map (comp (fn [s] (string/replace s #"-" " "))
                      string/capitalize
                      name)
                ks)]
    [tbody (map (apply juxt ks) rows)]]))
