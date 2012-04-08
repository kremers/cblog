(ns cblog.security (:use [sandbar core stateful-session auth form-authentication validation] [cblog db]))

(def security-policy
       [#".*\.(css|js|png|jpg|gif|ico)$" :any
        #"/admin/bootstrap"           :any
        #"/admin.*"                   :admin 
        #"/permission-denied.*"       :any
        #"/login.*"                   :any 
        #"/logout.*"                  :any
        #"/index.html"                :any
        #"/"                          :any
        #".*"                         #{:admin :user}])

(defrecord DemoAdapter [] 
    FormAuthAdapter
    (load-user [this username password]
         (let [login {:username username :password password}]
                (cond (= username "admin")        (merge login {:roles #{:admin}})
                :else login)))
    (validate-password
         [this]
         (fn [m]
                (if-not (empty? (dbauth (:username m) (:password m)))
                         m
                         (add-validation-error m "Username and password do not match!")))))

(defn form-authentication-adapter [] (DemoAdapter.))


