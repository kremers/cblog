(ns cblog.admin (:import [org.bson.types ObjectId]) 
  (:use [cblog.db] [monger.collection :only [find-maps find-map-by-id find-one-as-map insert]]
                      [monger.operators] [clojure.tools.logging :only (info error)]))
; 1. Generate a map with all required entries for the administrator pages
; 2. Provide functionality to handle CRUD and more

(defn posts-overview [] 
  (let [posts (vec (find-maps "posts")) 
        sum (count posts)
        active (count (filter #(true? (:active %)) posts)) 
        inactive (- sum active)] 
    {:posts posts :sum sum :active active :inactive inactive}))


; Check if "id" is set,... => update
(defn save-post [params]
  (println (pr-str params))
  (let [post 
         (+post { :title (params "title") 
                :category (params "category")
                :content (params "content")})] 
   (if-let [draft (= (params "save") "Save Draft")] 
     (insert "posts" post) 
     (insert "posts" (assoc post :active true)))))

(defn prepare-edit [params]
  (let [categories (vec (find-maps "categories"))]
  (if-let [id (params "id")] 
    (let [post (find-map-by-id "posts" (ObjectId. id))] 
      { :post post :categories (map #(if (= (:category post) (:name %1)) (assoc %1 :selected true) %1) categories) })
  {:categories categories} ))
)


