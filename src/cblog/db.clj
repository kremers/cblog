(ns cblog.db
  (:require [monger.query :as q])
  (:use [sandbar.auth] [cblog.util] [monger.core :only [connect! connect set-db! get-db]]
        [monger.collection :only [any? find-maps insert find-one-as-map map-reduce]] [monger.operators]
        [monger.conversion]
        [stencil.core]
        [clojure.string :only [lower-case]]
        [clj-time.format :only [parse unparse formatters]]
        [validateur.validation] [clojure.tools.logging :only (info error)]))

(def dbname "cblog")
(defn connect-to-db! [] (connect!) (monger.core/set-db! (monger.core/get-db dbname)))

(defn +user [& [init]] 
  (merge {:login nil :pass nil :nicename nil :email nil 
          :url nil :created (gen-timestamp) :active false} init))
(defn +post [& [init]] 
  (merge {:title nil :content nil :active false :author nil 
          :created (gen-timestamp) :lastmodified nil :category nil 
          :tags nil :showtitle true :markdown nil} init))
(defn +category [& [init]] 
  (merge {:name nil :urlfriendly nil :lastmodified (gen-timestamp)} init))
(defn +settings [& [init]] 
  (merge {:blogtitle "c(lojure) blog" :timezone nil :version 0 
          :askimetapikey nil :metadesc nil :metakeywords nil 
          :metaauthor nil :analyticsaccountkey nil 
          :s3accesskey nil :s3secretkey nil 
          :s3bucketname nil :language "en-gb"} init))

(defn +link [& [init]] (merge {:name nil :address nil :desc nil :target "_blank" :rating "0" :category nil})) 

(defn count-tags [] (let [taglist (q/with-collection "posts" (q/find {:active true :category {$ne "Welcome"}}) (q/fields [:tags]))]
          (sort-by #(lower-case (:name %) ) (map (fn [[x y]] {:name x :count y} ) (frequencies (filter #(not= % nil) (flatten (map #(:tags %) taglist))))))))

(defn valid-user? [user] (let [v (validation-set (presence-of :login) (presence-of :pass)) ] (valid? v user)))

; for example to assure outgoing links to open new windows ,...
(defn post-postprocess [text] text)

(defn settings-overview [] (find-one-as-map "settings" {:version 0}))

(defn dbauth [user pass] (find-one-as-map "users" {:login user :pass pass}))

(defn all-categories [] (find-maps "categories"))

(defn read-posts    ([] (read-posts 0))
                    ([limit & [additional]] (for [x (q/with-collection "posts" (q/find (merge {:active true} additional)) 
                              (q/sort { :created -1 }) (q/limit limit)) 
                           y (map #(select-keys % [:urlfriendly :name] ) (all-categories)) 
                           :let [ z (merge x { :urlfriendly (y :urlfriendly) 
                                               :urlfriendtitle (urlfriend (x :title)) 
                                               :RFC822created  (unparse (formatters :rfc822) (parse (formatters :date-hour-minute-second) (x :created)))
                                               :excerpt (let [c (x :content)] (if (> (count c) 100) (str (subs c 100) " [...]") c)) } ) ] 
                           :when (= (x :category) (y :name))] z)))

(defn read-posts-but-skip-welcome-page [num] (read-posts num {:category {$ne "Welcome"} }))

(defn recentposts [] (read-posts-but-skip-welcome-page 5))

(defn posts-by-category [category] (reverse (sort-by :created (read-posts 0 {:category category}))))

(defn posts-by-urlfriendly-category [urlfriendly]
    (let [category (:name (find-one-as-map "categories" {:urlfriendly urlfriendly}))] (posts-by-category category)))

(defn readpost [urlfriendcat, urlfriendtitle] 
  (let [found (filter #(= urlfriendtitle (urlfriend (:title %))) (posts-by-urlfriendly-category urlfriendcat))] (first found)))

(defn posts-by-tag [tag] (filter #(in-coll? tag (:tags %)) (read-posts-but-skip-welcome-page)))

(defn basicinfo [] {:headercategories (vec (filter #(not= (:name %) "Welcome") (find-maps "categories")))
                    :username (current-username)
                    :recentposts (recentposts)
                    :settings (find-one-as-map "settings" {:version 0}) })

(defn bootstrap-database []
 (let [admin-exists    (any? "users" {:login "admin"}) 
       welcome-exists  (any? "categories" {:name "Welcome"})
       post-exists     (any? "posts" {:category "Welcome"})
       settings-exists (any? "settings" {:version 0})] 
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
                               posts    (seq (read-posts-but-skip-welcome-page 0))] 
                           {:settings settings :posts posts} )) 
(defn render-atomfeed [] (render-file "templates/feed/atom" (prepare-feed)))
(defn render-rssfeed  [host] (render-file "templates/feed/rss" (merge (prepare-feed) {:host host})))


