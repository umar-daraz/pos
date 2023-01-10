(ns pos.auth
  (:require [buddy.auth :as auth]
            [buddy.auth.backends :as backends]))

(defn basic-authfn [req authdata]
  (let [username (:username authdata)
        password {:password authdata}]
    (println authdata)
    username))

(def basic-backend (backends/basic {:realm "petApp"
                                    :authfn basic-authfn}))
