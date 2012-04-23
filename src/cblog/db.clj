(ns cblog.db
  (:require [monger.query :as q])
  (:use [sandbar.auth] [cblog.util] [monger.core :only [connect! connect set-db! get-db]]
        [monger.collection :only [find-maps insert find-one-as-map]] [monger.operators]
        [monger.conversion]
        [stencil.core]
        [validateur.validation] [clojure.tools.logging :only (info error)]))

(def dbname "cblog")
(defn connect-to-db! [] (connect!) (monger.core/set-db! (monger.core/get-db dbname)))

(defn +user [& [init]] 
  (merge {:login nil :pass nil :nicename nil :email nil 
          :url nil :created (gen-timestamp) :active false} init))
(defn +post [& [init]] 
  (merge {:title nil :content nil :active false :author nil 
          :created (gen-timestamp) :lastmodified nil :category nil 
          :tags nil :showtitle true } init))
(defn +category [& [init]] 
  (merge {:name nil :urlfriendly nil :lastmodified (gen-timestamp)} init))
(defn +settings [& [init]] 
  (merge {:blogtitle "c(lojure) blog" :timezone nil :version 0 
          :askimetapikey nil :metadesc nil :metakeywords nil 
          :metaauthor nil :analyticsaccountkey nil 
          :s3accesskey nil :s3secretkey nil} init))

(defn valid-user? [user] (let [v (validation-set (presence-of :login) (presence-of :pass)) ] (valid? v user)))

; for example to assure outgoing links to open new windows ,...
(defn post-postprocess [text] text)

(defn settings-overview [] (find-one-as-map "settings" {:version 0}))

(defn dbauth [user pass] (find-one-as-map "users" {:login user :pass pass}))

(defn posts-by-category [category] 
  (reverse (sort-by :created (for [x (find-maps "posts" {:category category :active true})] (update-in x [:content] post-postprocess)))))

(defn posts-by-urlfriendly-category [urlfriendly] 
  (let [category (:name (find-one-as-map "categories" {:urlfriendly urlfriendly}))] (posts-by-category category)))

(defn all-categories []  
  (find-maps "categories"))

(defn read-posts    ([] (read-posts 0))
                    ([limit] (for [x (q/with-collection "posts" (q/find {:active true :category {$ne "Welcome"} }) 
                              (q/sort { :created -1 }) (q/limit limit)) 
                           y (map #(select-keys % [:urlfriendly :name] ) (all-categories)) 
                           :let [ z (merge x { :urlfriendly (y :urlfriendly) :urlfriendtitle (urlfriend (x :title)) } ) ] 
                           :when (= (x :category) (y :name))] z)))

(defn recentposts [] (read-posts 5))


(defn readpost [urlfriendcat, urlfriendtitle] 
  (let [found (filter #(= urlfriendtitle (urlfriend (:title %))) (posts-by-urlfriendly-category urlfriendcat))] (first found)))

(defn exists? [collection param] 
  (not (empty? (find-one-as-map collection param))))

(defn basicinfo [] {:headercategories (vec (filter #(not= (:name %) "Welcome") (find-maps "categories")))
                    :username (current-username)
                    :recentposts (recentposts)
                    :settings (find-one-as-map "settings" {:version 0}) })

(defn bootstrap-database []
 (let [admin-exists    (exists? "users" {:login "admin"}) 
       welcome-exists  (exists? "categories" {:name "Welcome"})
       post-exists     (exists? "posts" {:category "Welcome"})
       settings-exists (exists? "settings" {:version 0})] 
   (dorun (map #(info (str (key %) (val %))) {"exists(admin): " admin-exists 
                                              "exists(welcome): " welcome-exists 
                                              "exists(firstpost): " post-exists
                                              "exists(settings): " settings-exists}))
   (if (not admin-exists)   (do (info "init users") 
                              (insert "users" (+user {:login "admin" 
                                                      :pass (hash-password "admin" "hawaiian black salt") 
                                                      :nicename "Administrator" 
                                                      :active true}))) nil)
   (if (not welcome-exists) (do (info "init category") 
                              (insert "categories" (+category {:name "Welcome" 
                                                               :nicename "Landing page" 
                                                               :lastmodified (gen-timestamp)}))) nil)
   (if (not post-exists)    (do (info "init welcome post") 
                              (insert "posts" (+post {:title "This is cblog" 
                                                      :content "A real clojure blog written by Martin Kremers" 
                                                      :active true 
                                                      :author "admin" 
                                                      :category "Welcome"}))) nil)
   (if (not settings-exists) (do (info "init settings")
                               (insert "settings" (+settings {}))) nil))
)


(defn prepare-feed    [] (let [settings (settings-overview)
                               posts    (seq (read-posts))] 
                           {:settings settings :posts posts} )) 
(defn render-atomfeed [] (render-file "templates/feed/atom" (prepare-feed)))
(defn render-rssfeed  [] (render-file "templates/feed/rss"  (prepare-feed)))


;{:Feed {:Author :Title :Description :Link :Language :CopyrightInfo :PubDate :LastBuildDate :Items {:Title :Summary :link :guid :pubDate}} } 

