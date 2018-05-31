(ns stand-lib.handlers
  (:require
   [re-frame.core :as rf]
   [stand-lib.re-frame.utils :refer [query]]
   [stand-lib.utils :refer [extract-ns+name make-keys]]))

(defn query-sub [db [event ns+name]]
  (get-in db (make-keys ns+name)))

(defn get-value [event]
  (-> event .-target .-value))

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
 :set-state
 (fn [db [_ path val]]
   (assoc-in db (make-keys path) val)))

(rf/reg-event-db
 :update-state
 (fn [db [_ path f]]
   (update-in db (make-keys path) f)))

; ------------------------------------------------------------------------------
; Subs
; ------------------------------------------------------------------------------

(rf/reg-sub :query query-sub)
