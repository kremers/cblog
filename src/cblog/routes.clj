(ns cblog.routes
  (:use [ring.middleware file file-info params reload]
        [net.cgrand.moustache :only [app]]
        [ring.util.response :only [response redirect header]]
        [stencil.core]
        [cblog util security admin db]
        [compojure core]
        [sandbar.form-authentication] [sandbar.validation] [sandbar.stateful-session] [sandbar.auth]
  ))


(defn envelope [content] (utf8response (render-file "templates/default"  (merge {:capsule content} (basicinfo)))))

(defroutes my-routes 
    (form-authentication-routes (fn [_ c] (layout c)) (form-authentication-adapter))
    (GET  "/" [] (envelope (render-file "templates/main" {:posts (vec (posts-by-category "Welcome"))}) ))
    (GET  "/admin" [] (redirect "/admin/"))
    (GET  "/admin/" [] (envelope (render-file "templates/admin" nil)))
    (GET  "/admin/posts" [] (redirect "/admin/posts/"))
    (GET  "/admin/posts/" [] (envelope (render-file "templates/admin_posts" (posts-overview))))
    (GET  "/admin/posts/edit" {params :params} (envelope (render-file "templates/admin_editpost" (prepare-edit params))))
    (POST "/admin/posts/remove" request (utf8response (remove-post request)))
    (POST "/admin/posts/save" {params :params} (envelope (save-post params)))
    (GET  "/admin/categories" [] (redirect "/admin/categories/"))
    (GET  "/admin/categories/" [] (envelope (render-file "templates/admin_categories" (categories-overview))))
    (POST "/admin/categories/save" request (utf8response (save-category request)))
    (POST "/admin/categories/remove" request (utf8response (remove-category request)))
    (GET  "/admin/bootstrap" [] (do (bootstrap-database) (utf8response "bootstraped! See log for details.")))
    (ANY  "*" [] (utf8response (make-404)))
)

(defn init-routes! [] (app 
  (-> my-routes 
    (with-security security-policy form-authentication)
    wrap-params
    wrap-file-info
    wrap-stateful-session
    (wrap-file "resources")
)))


