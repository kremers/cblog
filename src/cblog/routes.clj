(ns cblog.routes
  (:use [ring.middleware content-type file file-info params reload]
        [net.cgrand.moustache :only [app]]
        [ring.util.response :only [content-type response redirect header]]
        [stencil.core]
        [clojure.tools.logging :only (info error)]
        [cblog util security admin db media tagcloud]
        [compojure core]
        [kremers.monger-session]
        [sandbar.form-authentication] [sandbar.validation] [sandbar.stateful-session] [sandbar.auth]
  ))


(defn envelope
  [content] (utf8response (render-file "templates/default"
                             (merge {:capsule content} (basicinfo) {:tagcloud (tagcloud)} {:links (vec (get-links))}))))
(defn adminui
  [content] (utf8response (render-file "templates/default_adminui" 
                             (merge {:capsule content} 
                                    (basicinfo) 
                                    {:adminmenue (render-file "templates/adminmenue" nil)}))))

(defroutes my-routes 
    (form-authentication-routes (fn [_ c] (layout c)) (form-authentication-adapter))
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
    (GET  "/admin/bootstrap" [] (do (bootstrap-database) (utf8response "bootstraped! See log for details.")))
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
    (GET  "/tag/:tag" [tag] (envelope (render-file "templates/main" {:posts (vec (posts-by-tag tag)) })))
    (GET  ["/cdn/:key" :key #".+"] [key] (media_redirect key))
    (GET  "/:category" [category]  (envelope (render-file "templates/main" {:posts (vec (posts-by-urlfriendly-category category))})))
    (GET  "/:category/:post" [category, post] (envelope (render-file "templates/showpost" (readpost category post))))
    (ANY  "*" [] (utf8response (make-404)))
)

(defn wrap-context-uri [handler] (fn [request] (handler (assoc request :host (str "http://" (:server-name request) ":" (:server-port request)) ))))

(defn init-routes! [] (app 
  (-> my-routes 
    (with-security security-policy form-authentication)
    wrap-params
    wrap-context-uri
    (wrap-stateful-session {:store (mongodb-store)})
    (wrap-file "resources")
    wrap-file-info
)))


