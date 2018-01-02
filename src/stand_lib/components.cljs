(ns stand-lib.components
  (:require
   [reagent.core :as r :refer [atom]]
   [re-frame.core :as rf]
   [stand-lib.handlers]
   [stand-lib.re-frame.utils :refer
    [extract-ns+name make-keys set-state update-state]]))

(defn query-sub [db [event ns+name]]
  (get-in db (extract-ns+name ns+name)))

; ------------------------------------------------------------------------------
; Subs
; ------------------------------------------------------------------------------

(rf/reg-sub :query query-sub)


; ------------------------------------------------------------------------------
; Handlers
; ------------------------------------------------------------------------------

(rf/reg-event-db
 :set-state
 (fn [db [_ ks val]]
   (assoc-in db ks val)))

(rf/reg-event-db
 :update-state
 (fn [db [_ ks f]]
   (update-in db ks f)))

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

(defn set-state [ks val]
  (rf/dispatch [:set-state ks val]))

(defn update-state [ks f]
  (rf/dispatch [:update-state ks f]))

(defn set-state-callback [ns+name]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (set-state ks (-> comp .-target .-value (js->clj :keywordize-keys true))))))

(defn set-state-callback-for-text [ns+name]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (set-state ks (-> comp .-target .-value)))))

(defn set-state-with-value-callback [ns+name val]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (set-state ks val))))

(defn update-state-callback [ns+name f]
  (let [ks (make-keys ns+name)]
    (fn [comp]
      (update-state ks f))))

(defn get-component-value
  "Returns a Reagent `reaction`, after querying the app-db for `name`.
  `name` is a keyword, qualified or not, referring to the value's path
  in app-db."
  [name]
  (rf/subscribe [:query name]))

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

; NOTE: inputs type `:text` and `:email` fall in the default
(defmethod input :default
  [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback-for-text (:name attrs))))]
    [:input edited-attrs]))

(defmethod input :number
  [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback (:name attrs))))]
    [:input edited-attrs]))

; By default the checkbox state is designed to be stored in a single set.
; By default we figure out if the checkbox is checked based on its value's
; presence in that set.
; To override this behavior one must roll their own :on-change and :checked
; attributes.
(defmethod input :checkbox
  [attrs]
  (let [acc (get-component-value (:name attrs))
        f (fn [acc]
            (let [val (:value attrs)]
              (cond
                (nil? acc) #{val}
                (contains? acc val) (disj acc val)
                :default (conj acc val))))
        edited-attrs
        (-> attrs
            (update :on-change #(or % (update-state-callback (:name attrs) f))))]
            ; (update :checked
            ;         #(or %
            ;              (when (contains? @acc (:value attrs))
            ;                true))))]
    ;; persist value when input is default-checked
    (when (and (:default-checked edited-attrs)
               (not (contains? @acc (:value edited-attrs))))
      ((:on-change edited-attrs)))
    [:input edited-attrs]))

; The :value attribute is used so we don't need to bother coercing the
; e.target.value to its original type.
(defmethod input :radio
  [attrs]
  (let [acc (get-component-value (:name attrs))
        edited-attrs (update attrs :on-change #(or % (set-state-with-value-callback (:name attrs) (:value attrs))))]
    ;; persist value when input is default-checked
    (when (and (:default-checked edited-attrs)
               (not (contains? @acc (:value edited-attrs))))
      ((:on-change edited-attrs)))
    [:input edited-attrs]))

(defmethod input :date
  [attrs]
  (let [edited-attrs  (update attrs :on-change #(or % (set-state-callback (:name attrs))))]
    [:input edited-attrs]))

(defmethod input :file
  [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback (:name attrs))))]
    [:input edited-attrs]))

(defn textarea [attrs]
  (let [edited-attrs (update attrs :on-change #(or % (set-state-callback-for-text (:name attrs))))]
    [:textarea edited-attrs]))

(defn select [attrs & options]
  (let [ks (make-keys (:name attrs))
        ;; get the :value of this first option component
        default-val (-> options ffirst second :value)
        edited-attrs
        (-> attrs
            (update :on-change #(or % (set-state-callback (:name attrs))))
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

            :else [input edited-attrs])])))])


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
      [:th th])]])

(defn tbody [rows]
  (into
   [:tbody]
   (for [row rows]
     (into
      [:tr]
      (for [td row]
        [:td td])))))

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
  ([ks rows {:keys [class]}]
   [:table
    {:class class}
    ;; if there are extra headers we append them
    [thead (map (comp (fn [s] (string/replace s #"-" " "))
                      string/capitalize
                      name)
                ks)]
    [tbody (map (apply juxt ks) rows)]]))
