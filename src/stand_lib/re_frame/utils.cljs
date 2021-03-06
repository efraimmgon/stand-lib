(ns stand-lib.re-frame.utils
  (:require
   [re-frame.core :as rf]
   [stand-lib.utils :refer [extract-ns+name]]))

(defn query
  "Meant to be used with a rf/reg-sub. Takes a `db` map and an event-id from
   a rf/dispatch and gets the resource based on the namespaced id.
   Ids are namespaced by `.`, eg: `admin.background-image`"
  [db [event-id]]
  (let [event-ks (extract-ns+name event-id)]
    (get-in db event-ks)))

(defn <sub [query-v]
  (deref (rf/subscribe query-v)))

(defn set-state [ks val]
  (rf/dispatch [:set-state ks val]))

(defn update-state [ks f]
  (rf/dispatch [:update-state ks f]))
