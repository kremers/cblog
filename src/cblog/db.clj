(ns cblog.db
  (:use [monger.core :only [connect! connect set-db! get-db]]
        [monger.collection :only [find-maps]] [monger.operators]))

(def dbname "cblog")
(defn connect-to-db! [] (connect!) (monger.core/set-db! (monger.core/get-db dbname)))
(defn +user [& [params]] (merge {:login nil :pass nil :nicename nil :email nil :url nil :registrationdate nil :active false} params))
(defn +post [& [params]] (merge  {:pname nil :title nil :content nil :active false :author nil :created nil :lastmodified nil} params))
(defn dbauth [user pass] (find-maps "users" {:login user :pass pass}))
(defn +category [& [params]] (merge {:name nil :nicename nil :lastmodified nil} params))

; Example how to add users
; (insert "users" (+user {:login "user" :pass "user" :nicename "mruser" :active true}))


