(ns cblog.admin 
  (:import [org.bson.types ObjectId]) 
  (:use [cblog db util] [cheshire.core] [sandbar.auth] 
        [monger.collection :only [find-maps update remove-by-id update-by-id find-map-by-id find-one-as-map insert]]
        [monger.operators] [clojure.tools.logging :only (info error)]))

(defn update-settings [req] (let [p (json-in req) k (:key p) v (:value p) old (settings-overview)] 
    (if (contains? (+settings) (keyword k)) (:err (update "settings" {:version 0} (merge old {(keyword k) v}))) "key not available")) )

(defn update-adminpw [req] (when-let [isAdmin (= (current-username) "admin")] 
                             (:err (let [p (json-in req) k (:newpw p) oldAdmin (find-one-as-map "users" {:login "admin"})] 
                                     (do (info (str "Admin password changed to: " k)) 
                                         (update "users" {:login "admin"} (merge oldAdmin {:pass (hash-password k "hawaiian black salt")})))))))
(defn posts-overview [] 
  (let [posts (vec (find-maps "posts")) 
        sum (count posts)
        active (count (filter #(true? (:active %)) posts)) 
        inactive (- sum active)] 
    {:posts posts :sum sum :active active :inactive inactive}))

; Needs refactoring, :... section far too long
(defn save-post [params]
  (let [ draft? (= (params "save") "Save Draft")
        post   (+post { :title (params "title")
                       :author (current-username)
                       :category (params "category")
                       :content (params "content")
                       :tags    (params "tags[]")
                       :showtitle (= "on" (params "showtitle"))
                       :active (not draft?)})]  
    (if-let [id (params "postid")] (update-by-id "posts" (ObjectId. id) post) (insert "posts" post))))

(defn prepare-edit [params]
  (let [categories (vec (find-maps "categories"))]
    (if-let [id (params "id")] 
      (let [post (find-map-by-id "posts" (ObjectId. id))] 
        { :post post :categories (map #(if (= (:category post) (:name %1)) (assoc %1 :selected true) %1) categories) })
      {:categories categories} )))

(defn json-embed "Embeds a sexp in a json response"
  [q] (generate-string {:result (str q)}))
(defn merge-by "Takes a map data and a map allowed as attribute, returning data filtered by allowed"
  [data, allowed] (merge allowed (select-keys data (keys allowed))))
(defn overview "Reads all objects from the collection as map"
  [#^String collection] {:categories (vec (find-maps collection))} )
(defn delete-by-id "Deletes a entity by a :id field in a json structure from a specific collection"
  [req #^String collection] (generate-string {:result (str (remove-by-id collection (ObjectId. (:id (json-in req)))))})) 
(defn save-new "Creates and inserts a new entity in a specified collection. Parameters get checked against map keys"
  [req #^String collection keymap] (json-embed (let [data (json-in req)] (insert collection (merge-by data keymap)))))
(defn edit-by-id "Modifies an item by key"
  [req allowed] (json-embed (let [data (json-in req) id (:id data)] (update-by-id "categories" (ObjectId. id) (merge-by data allowed)))))

(defn categories-overview [] (overview "categories"))
(defn remove-category [req] (delete-by-id req "categories"))
(defn save-category [req] (save-new req "categories" (+category)))
(defn remove-post [req] (delete-by-id req "posts"))
(defn remove-link [req] (delete-by-id req "links"))
(defn update-category [req] (edit-by-id req (+category)))

(defn admin-health [] (generate-string (let [^Runtime r (Runtime/getRuntime)] 
                                         { :freememory (int (/ (. r (freeMemory)) 1e6))
                                          :totalmemory (int (/ (. r (totalMemory)) 1e6)) 
                                          :maxmemory (int (/ (. r (maxMemory)) 1e6))
                                          :processors (int (. r (availableProcessors))) })))