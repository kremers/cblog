(ns cblog.admin (:import [org.bson.types ObjectId]) 
  (:use [cblog db util] [cheshire.core] [sandbar.auth] [monger.collection :only [find-maps remove-by-id update-by-id find-map-by-id find-one-as-map insert]]
                      [monger.operators] [clojure.tools.logging :only (info error)]))

(defn posts-overview [] 
  (let [posts (vec (find-maps "posts")) 
        sum (count posts)
        active (count (filter #(true? (:active %)) posts)) 
        inactive (- sum active)] 
    {:posts posts :sum sum :active active :inactive inactive}))


; Check if "id" is set,... => update
(defn save-post [params]
  (let [ draft? (= (params "save") "Save Draft")
         post   (+post { :title (params "title")
                         :author (current-username)
                         :category (params "category")
                         :content (params "content")
                         :active (not draft?)})]  
  (if-let [id (params "postid")] (update-by-id "posts" (ObjectId. id) post) (insert "posts" post))))

(defn remove-post [req] (generate-string {:result (str (remove-by-id "posts" (ObjectId. (:id (json-in req)))))}))

(defn prepare-edit [params]
  (let [categories (vec (find-maps "categories"))]
  (if-let [id (params "id")] 
    (let [post (find-map-by-id "posts" (ObjectId. id))] 
      { :post post :categories (map #(if (= (:category post) (:name %1)) (assoc %1 :selected true) %1) categories) })
  {:categories categories} ))
)


