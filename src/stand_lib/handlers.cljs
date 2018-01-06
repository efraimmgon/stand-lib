(ns stand-lib.handlers
  (:require
   [re-frame.core :as rf]
   [stand-lib.re-frame.utils :refer [query]]
   [stand-lib.utils :refer [extract-ns+name]]))

; ------------------------------------------------------------------------------
; Event handlers
; ------------------------------------------------------------------------------

(rf/reg-event-fx
 :dispatch-n
 (fn [_ [_ events]]
   (doseq [evt events]
     (rf/dispatch evt))
   nil))

(rf/reg-event-db
 :set
 (fn [db [_ ns+name val]]
   (assoc-in db (extract-ns+name ns+name) val)))

; ------------------------------------------------------------------------------
; Subs
; ------------------------------------------------------------------------------

(rf/reg-sub :error query)
