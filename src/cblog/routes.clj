(ns cblog.routes
  (:use [ring.middleware file file-info params reload]
        [net.cgrand.moustache :only [app]]
        [ring.util.response :only [response redirect header]]
        [stencil.core]
        [cblog util security admin db]
        [compojure core]
        [kremers.monger-session]
        [sandbar.form-authentication] [sandbar.validation] [sandbar.stateful-session] [sandbar.auth]
  ))


(defn envelope [content] (utf8response (render-file "templates/default"  (merge {:capsule content} (basicinfo)))))
(defn adminui  [content] (utf8response (render-file "templates/default_adminui" 
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
    (GET  "/admin/links/" [] (adminui (render-file "templates/admin_links" {}))) 
    (GET  "/admin/media"  [] (redirect "/admin/media/"))
    (GET  "/admin/media/" [] (adminui (render-file "templates/admin_media" {})))
    (GET  "/feed/atom"    [] (render-atomfeed))
    (GET  "/feed"         [] (render-rssfeed))
    (GET  "/:category" [category]  (envelope (render-file "templates/main" {:posts (vec (posts-by-urlfriendly-category category))})))
    (GET  "/:category/:post" [category, post] (envelope (render-file "templates/showpost" (readpost category post))))
    (ANY  "*" [] (utf8response (make-404)))
)

(defn init-routes! [] (app 
  (-> my-routes 
    (with-security security-policy form-authentication)
    wrap-params
    wrap-file-info
    (wrap-stateful-session {:store (mongodb-store)})
    (wrap-file "resources")
)))


