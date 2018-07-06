(ns stand-lib.comps.forms
  (:require
   [cljs.reader :as reader]
   [re-frame.core :as rf]
   [stand-lib.utils.forms :refer
    [handle-mopt-change-at set-state! toggle-value! target-value]]))

; I have started this project after not being satisfied with the available
; projects. Also, following Lisp's philosiphy, I just wanted to hack something
; myself.

; A couple of projects that I wasn't completely satisfied with, but that have
; some very nice ideas which I borrowed were reagent-forms
; (https://github.com/reagent-project/reagent-forms) and free-form
; (https://github.com/pupeno/free-form).
; The one thing I disliked about both of them is that one must learn yet
; another domain specific language (DSL). The thing about DSL's is that
; they have quite a learning curve when you want to do unusual things.
; Instead, I wanted to do what one did with HTML, but with ClojureScript.
; Therefore, we'll leverege as much of HTML syntax as possible, so one can
; use all the attributes that we were already used to when handling with
; inputs with JavaScript.

; Levereging JavaScript's capabilities, event.target.type always returns
; a string. To work around this limitation, we're levereging
; `cljs.reader/read-string` to read the returned string to its respective
; Clojure type.

; Although different input types do different things, the core of the
; implementation revolves around the `on-change` fn.
; Given my personal belief that re-frame is essential for SPA development
; using reagent, our implementation relies heavily on it.

; With the intention of allowing users to tailor the components to their
; uses, custom values can be provided to some attributes
; In all cases, a custom `on-change` fn can be provided. Other custom values
; can be provided, as it makes sense for each input type.

; Suppose we want to store the text users type as all caps, and display
; the text as all lower case. For that end we have only to customize
; the `on-change` and `value` keys:
; [input {:type :text
;         :name :users.user/name
;         :on-change
;         (fn [e] (set-state! :users.user/name (upper-case (target-value e))))}]
;         :default-value "Default text."
;         :value
;         (lower-case @(rf/subscribe [:query :users.user/name]))}]

; NOTE:
; In React default-value and default-checked keys are meant for uncontrolled
; components. Since our comps are controlled this does not matter for us.
; Therefore for our project those keys have the meaning of displaying the
; default value/option while also persisting its location inside
; re_frame.db.app_db.state.
; While not completely certain about this choice, at this time it seems the
; best approach.

; Given our implementation choice, the `:name` field must refer to a
; `re_frame.db.app_db.state` location.
; It can be given as a keyword, `:a.db.location` or  `:a.db/location`,
; or a vector of keywords, `[:a :db :location]`.
; I.e:
; [input {:name :users.user/name ; or :users.user.name or [:users :user :name]
;         :type :text
;         :required true}]

(defmulti input :type)

(defn- read-target-value [event coerce?]
  (let [sval (target-value event)]
    (if coerce?
      (reader/read-string sval)
      sval)))

(defn- clean-attrs [attrs]
  (dissoc attrs :default-value :default-checked :coerce?))

; `:default-value` will display the value at the input field and persist it
; at the location provided in `:name`.
; `:text`, `:password`
; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `:default-value`, `:coerce?`.
(defmethod input :default
  [attrs]
  (let [{:keys [default-value name coerce?],
         :or {coerce? false}} attrs
        stored-val (rf/subscribe [:query name])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (set-state! name (read-target-value e coerce?)))))
            ;; Reason for `""`: https://zhenyong.github.io/react/tips/controlled-input-null-value.html
            (update :value #(or % @stored-val ""))
            clean-attrs)]
    (when (and (nil? @stored-val) default-value)
      (set-state! name default-value))
    [:input edited-attrs]))

; `:default-value` will display the value at the input field and persist it
; at the location provided in `:name`
; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `:default-value`, `:coerce?`.
(defmethod input :number
  [attrs]
  (let [{:keys [default-value name coerce?],
         :or {coerce? true}} attrs
        stored-val (rf/subscribe [:query name])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (set-state! name (read-target-value e coerce?)))))
            ;; Reason for empty string: https://zhenyong.github.io/react/tips/controlled-input-null-value.html
            (update :value #(or % @stored-val ""))
            clean-attrs)]
    ;; Persist value when it's the default:
    (when (and (nil? @stored-val) default-value)
      (set-state! name default-value))
    [:input edited-attrs]))


; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `:checked`, `:default-checked`, `:coerce?`.
(defmethod input :radio
  [attrs]
  (let [{:keys [default-checked name value coerce?],
         :or {coerce? true}} attrs
        stored-val (rf/subscribe [:query name])
        value (if coerce? value (str value))
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (set-state! name (read-target-value e coerce?)))))
            ;; Reason for `false`: https://zhenyong.github.io/react/tips/controlled-input-null-value.html
            (update :checked #(or % (= value @stored-val)))
            clean-attrs)]
    ;; Persist value when it's the default:
    (when (and (nil? @stored-val) default-checked)
      (set-state! name value))
    [:input edited-attrs]))

; Like the other input components, the value is mapped to the location of the
; :name attr but the value assoced with it will only be either `true`
; or `false`.
; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `:checked`, `default-checked`, `:coerce?`.
(defmethod input :checkbox
  [attrs]
  (let [{:keys [default-checked name]} attrs
        stored-val (rf/subscribe [:query name])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (toggle-value! name))))
            ;; Reason for `false`: https://zhenyong.github.io/react/tips/controlled-input-null-value.html
            (update :checked #(or % @stored-val false))
            clean-attrs)]
    ;; Persist value when it's default-checked:
    (when (and (nil? @stored-val) default-checked)
      (toggle-value! name))
    [:input edited-attrs]))

; `:default-value` will display the value at the input field and persist it
; at the location provided in `:name`.
; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `:default-value`, `:coerce?`.
(defn textarea [attrs]
  (let [{:keys [default-value name coerce?]
         :or {coerce? false}} attrs
        stored-val (rf/subscribe [:query name])
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (set-state! name (read-target-value e coerce?)))))
            ;; Reason for empty string: https://zhenyong.github.io/react/tips/controlled-input-null-value.html
            (update :value #(or % @stored-val ""))
            clean-attrs)]
    ;; Persist default-value:
    (when (and (nil? @stored-val) default-value)
      (set-state! name default-value))
    [:textarea edited-attrs]))

; Required keys: `:name`, `:on-change`.
; Available fields: `:value`, `default-value`, `:coerce?`.
; NOTE: As per React, `:default-value` replaces the `selected` property.
; NOTE: If :multiple is `true`, `stored-val` will be a set of the selected
; options, otherwise, it will be the option's value (string, if :coerce? is false).
(defn select [attrs options]
  (let [{:keys [default-value multiple name coerce?]
         :or {coerce? true}} attrs
        stored-val (rf/subscribe [:query name])
        on-change (if multiple handle-mopt-change-at set-state!)
        edited-attrs
        (-> attrs
            (update :on-change #(or % (fn [e] (on-change name (read-target-value e coerce?)))))
            ;; Reason for empty string: https://zhenyong.github.io/react/tips/controlled-input-null-value.html
            (update :value #(or % @stored-val ""))
            clean-attrs)]
    ;; Persist value when it's the default:
    (when (and (nil? @stored-val) default-value)
        (set-state! name
                    (if multiple
                      #{default-value}
                      default-value)))
    [:select edited-attrs
     options]))
