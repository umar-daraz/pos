(ns pos.routes
  (:require [clojure.walk :as walk]
            [reitit.core :as r]
            #?(:cljs [reitit.frontend :as rf])))

(def routes
  [["/" {:name ::home
         :view ::home}]
   ["/about" {:name ::about
              :view ::about}]])

#?(:cljs
   (def *router (delay (r/router routes))))
