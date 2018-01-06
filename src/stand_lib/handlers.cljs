(ns stand-lib.handlers
  (:require
   [re-frame.core :as rf]
   [stand-lib.re-frame.utils :refer [query]]
   [stand-lib.utils :refer [extract-ns+name]]))

(defn query-sub [db [event ns+name]]
  (get-in db (extract-ns+name ns+name)))

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

(rf/reg-event-db
 :set-state
 (fn [db [_ ks val]]
   (assoc-in db ks val)))

(rf/reg-event-db
 :update-state
 (fn [db [_ ks f]]
   (update-in db ks f)))

; ------------------------------------------------------------------------------
; Subs
; ------------------------------------------------------------------------------

(rf/reg-sub :query query-sub)
