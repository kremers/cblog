(ns cblog.routes
  (:use [ring.middleware file file-info params reload]
        [net.cgrand.moustache :only [app]]
        [ring.util.response :only [response header]]
        [stencil.core]
        [cblog util security]
        [compojure core]
        [sandbar.form-authentication] [sandbar.validation] [sandbar.stateful-session] [sandbar.auth]
  ))

(defroutes my-routes 
    (GET "/" [] (utf8response (render-file "templates/default" {:capsule (render-file "templates/main" nil) })))
    (form-authentication-routes (fn [_ c] (layout c)) (form-authentication-adapter))
    (GET "/admin" [] (utf8response (render-file "templates/default" {:capsule (render-file "templates/admin" nil) })))
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


