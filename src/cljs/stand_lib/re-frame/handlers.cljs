(ns stand-lib.re-frame.handlers
  (:require
   [re-frame.core :as rf]
   [stand-lib.re-frame.utils :refer [query]]))

; ------------------------------------------------------------------------------
; Event handlers
; ------------------------------------------------------------------------------

(rf/reg-event-fx
 :ajax-error
 (fn [_ [_ response]]
   (js/console.log response)
   (rf/dispatch [:set-error (-> response :response :errors)])))

(reg-event-db
 :set-error
 (fn [db [_ error]]
   (assoc db :error error)))

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

(rf/reg-sub :error query)
