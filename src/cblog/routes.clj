(ns cblog.routes
  (:use [ring.middleware content-type file file-info params reload]
        [net.cgrand.moustache :only [app]]
        [ring.util.response :only [content-type response redirect header]]
        [stencil.core]
        [stencil.loader :only [unregister-all-templates]]
        [clojure.tools.logging :only (info error)]
        [cblog util admin db media tagcloud]
        [compojure core]
       ; [ring.middleware.etag :only [wrap-etag]]
        [ring.middleware.gzip :only [wrap-gzip]]
        [sandbar.form-authentication] [sandbar.validation] [sandbar.stateful-session] [sandbar.auth]
        [monger.ring.session-store :only [session-store]]
        [monger.collection :only [any?]]
  ))

(def development? (= "development" (get (System/getenv) "APP_ENV")))

(defn envelope 
  [content] (do (if development? (unregister-all-templates))
                (utf8response (render-file "templates/default" (merge {:capsule content} (basicinfo) {:tagcloud (tagcloud)} {:links (vec (get-links))})))))
(defn adminui
  [content] (utf8response (render-file "templates/default_adminui" 
                             (merge {:capsule content} 
                                    (basicinfo) 
                                    {:adminmenue (render-file "templates/adminmenue" nil)}))))

(defrecord simpleAdminAuth [] FormAuthAdapter
           (load-user [this username password] (let [login {:username username :password password}]
                                                 (cond (= username "admin") (merge login {:roles #{:admin}}) :else login)))
           (validate-password [this] (fn [m] (if-not (empty? (dbauth (:username m) (hash-password (:password m) "hawaiian black salt"))) m
                                                    (add-validation-error m "Username and password do not match!")))))

(defroutes my-routes 
    (form-authentication-routes (fn [_ c] (layout c)) (simpleAdminAuth.))
    (GET  "/" [] (envelope (render-file "templates/main" {:posts (vec (posts-by-category "Welcome"))}) ))
    (GET  "/admin" [] (redirect "/admin/"))
    (GET  "/admin/" [] (adminui (render-file "templates/admin" nil)))
    (GET  "/admin/posts" [] (redirect "/admin/posts/"))
    (GET  "/admin/posts/" [] (adminui (render-file "templates/admin_posts" (posts-overview))))
    (GET  "/admin/posts/edit" {params :params} (adminui (render-file "templates/admin_editpost" (prepare-edit params))))
    (POST "/admin/posts/remove" request (utf8response (remove-post request)))
    (POST "/admin/posts/save" {params :params} (adminui (save-post params)))
    (GET  "/admin/categories" [] (redirect "/admin/categories/"))
    (GET  "/admin/categories/" [] (adminui (render-file "templates/admin_categories" (categories-overview))))
    (POST "/admin/categories/save" request (jsonresp (save-category request)))
    (POST "/admin/categories/remove" request (jsonresp (remove-category request)))
    (POST "/admin/categories/update" request (jsonresp (update-category request)))
    (GET  "/bootstrap" [] (do (bootstrap-database) (utf8response "bootstraped! See log for details.")))
    (GET  "/admin/settings" [] (adminui (render-file "templates/admin_settings" (settings-overview))))
    (POST "/admin/settings/updatepartial" request (jsonresp (update-settings request)))
    (POST "/admin/settings/adminpwchange" request (jsonresp (update-adminpw  request)))
    (GET  "/admin/health" [] (jsonresp (admin-health)))
    (GET  "/admin/links"  [] (redirect "/admin/links/"))
    (GET  "/admin/links/" [] (adminui (render-file "templates/admin_links" {:links (vec (get-links))}))) 
    (POST "/admin/links/save" request (jsonresp (save-link request)))
    (POST "/admin/links/remove" request (jsonresp (remove-link request)))
    (GET  "/admin/media"  [] (redirect "/admin/media/"))
    (GET  "/admin/media/" [] (adminui (render-file "templates/admin_media" {})))
    (GET  "/admin/media/list" [] (content-type (response (medialist_json)) "application/json;charset=UTF-8" ))
    (POST "/admin/media/submit" request (do (handle-submit request) (response "{\"success\": true}" )))
    (POST "/admin/media/remove" request (do (delete-media request) (content-type (response "{\"success\": true}" ) "application/json;charset=UTF-8" )))
    (GET  "/feed"         request (response (render-rssfeed (:host request))))
    (GET  ["/blog/:key" :key #".+"] [key] (redirect  (str "/" (strip-trailing-slash key))))
    (GET  ["/tag/:tag"  :tag #".+"] [tag] (let [taggedposts (posts-by-tag tag)]
                              (if (empty? taggedposts) (make-404)
                               (envelope (render-file "templates/main" {:posts (vec taggedposts) })))))
    (GET  ["/cdn/:key" :key #".+"] [key] (media_redirect key))
    (GET  "/:category" [category]  (let [matching (vec (posts-by-urlfriendly-category category))]
                                     (if (any? "categories" {:urlfriendly category})
                                       (envelope (render-file "templates/main" {:posts matching}))
                                       (make-404))))
    (GET  "/:category/:post" [category, post] (if (empty? (readpost category post)) (make-404)
                                                  (envelope (render-file "templates/showpost" (readpost category post)))))
    (ANY  "*" [] (make-404)))

(def security-policy
  [#"/admin.*"                   :admin
   #".*\.(css|js|png|jpg|gif|ico)$" :any
   #"/admin/bootstrap"           :any
   #"/permission-denied.*"       :any
   #"/login.*"                   :any
   #"/logout.*"                  :any
   #"/index.html"                :any
   #"/"                          :any
   #".*"                         :any])

(defn wrap-context-uri [handler] (fn [request] (handler (assoc request :host (str "http://" (:server-name request) ":" (:server-port request)) ))))

(defn init-routes! [] (app  
  (-> my-routes 
    (with-security security-policy form-authentication)
    wrap-params
    wrap-context-uri
    (wrap-stateful-session {:store (session-store :sessions)})
    (wrap-file "resources")
    (wrap-file-info {"woff" "application/x-font-woff" "eot" "font/eot" })
;    wrap-etag
    wrap-gzip)))



