(ns cblog.routes
  (:use [ring.middleware file file-info params reload]
        [net.cgrand.moustache :only [app]]
        [ring.util.response :only [response redirect header]]
        [stencil.core]
        [cblog util security]
        [compojure core]
        [sandbar.form-authentication] [sandbar.validation] [sandbar.stateful-session] [sandbar.auth]
  ))


(defn envelope [content] (utf8response (render-file "templates/default"  {:capsule content})))

(defroutes my-routes 
    (form-authentication-routes (fn [_ c] (layout c)) (form-authentication-adapter))
    (GET "/" [] (envelope (render-file "templates/main" nil) ))
    (GET "/admin" [] (redirect "/admin/"))
    (GET "/admin/" [] (envelope (render-file "templates/admin" nil)))
    (GET "/admin/posts" [] (redirect "/admin/posts/"))
    (GET "/admin/posts/" [] (envelope (render-file "templates/admin_posts" nil)))
    (GET "/admin/posts/edit" [] (envelope (render-file "templates/admin_editpost" nil)))
    (ANY "*" [] (utf8response (make-404)))
)

(defn init-routes! [] (app 
  (-> my-routes 
    (with-security security-policy form-authentication)
    wrap-params
    wrap-file-info
    wrap-stateful-session
    (wrap-file "resources")
)))


