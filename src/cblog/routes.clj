(ns cblog.routes
  (:use [ring.middleware file file-info params reload]
        [net.cgrand.moustache :only [app]]
        [ring.util.response :only [response header]]
        [stencil.core]
        [cblog.util]
        [cblog.security]
        [hiccup core page-helpers]
        [compojure core]
        [sandbar.form-authentication] [sandbar.validation] [sandbar.stateful-session] [sandbar.auth]
  ))



(defn layout [content]
    (html
         (doctype :html4)
         [:html
              [:head
                    ;(stylesheet "sandbar.css")
                    ;(icon "icon.png")
              ]
              [:body
                    [:h2 "Sandbar Security Example"]
                    content
                    [:br]
                    [:div (if-let [username (current-username)]
                                         [:div
                                                        (str "You are logged in as " username ". ")
                                                        (link-to "logout" "Logout")])]]]))

(defn make-404 [] (str "404\n---\nCeci n'est pas une 404"))

(defroutes my-routes 
    (GET "/" [] (utf8response (render-file "templates/main" nil)))
    (form-authentication-routes (fn [_ c] (layout c)) (form-authentication-adapter))
    (ANY "*" [] (utf8response (make-404)))
)

(defn init-routes! [] (app (-> my-routes  
                   (with-security security-policy form-authentication)
                   wrap-params
                   wrap-file-info
                   wrap-stateful-session
                   (wrap-file "resources")
                   )))

