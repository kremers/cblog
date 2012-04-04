(ns cblog.admin (:import [org.bson.types ObjectId]) 
  (:use [cblog.db] [monger.collection :only [find-maps find-map-by-id find-one-as-map]]
                      [monger.operators] [clojure.tools.logging :only (info error)]))
; 1. Generate a map with all required entries for the administrator pages
; 2. Provide functionality to handle CRUD and more

(defn posts-overview [] 
  (let [posts (vec (find-maps "posts")) 
        sum (count posts)
        active (count (filter #(= (:active %)) posts)) 
        inactive (- sum active)] 
    {:posts posts :sum sum :active active :inactive inactive}))

(defn save-post [params]
    (pr-str params) )

; (find-map-by-id "posts" (ObjectId. ....)

(defn prepare-edit [params]
    (info params) 
    {:categories (vec (find-maps "categories"))}
  )
