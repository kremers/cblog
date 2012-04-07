(ns cblog.db
  (:use [monger.core :only [connect! connect set-db! get-db]]
        [monger.collection :only [find-maps find-one-as-map]] [monger.operators]
        [validateur.validation] [cblog.util]))

(def dbname "cblog")
(defn connect-to-db! [] (connect!) (monger.core/set-db! (monger.core/get-db dbname)))

(defn +user [& [init]] 
  (merge {:login nil :pass nil :nicename nil :email nil :url nil :created (gen-timestamp) :active false} init))
(defn +post [& [init]] 
  (merge {:title nil :content nil :active false :author nil :created (gen-timestamp) :lastmodified nil :category nil :tags nil } init))
(defn +category [& [init]] 
  (merge {:name nil :nicename nil :lastmodified nil} init))

(defn valid-user? [user] (let [v (validation-set (presence-of :login) (presence-of :pass)) ] (valid? v user)))

(defn dbauth [user pass] (find-one-as-map "users" {:login user :pass pass}))
(defn posts-by-category [category] (reverse (sort-by :created (find-maps "posts" {:category category :active true}))))
(defn all-categories [] (find-maps "categories"))

; Example how to add data
; (insert "users" (+user {:login "admin" :pass "admin" :nicename "Administrator" :active true}))
; (insert "categories" (+category {:name "Welcome" :nicename "Landing page" :lastmodified (gen-timestamp)}))
; (insert "posts" (+post {:title "This is cblog" :content "A real clojure blog written by Martin Kremers" :active true :author "admin" :category "Welcome"}))
