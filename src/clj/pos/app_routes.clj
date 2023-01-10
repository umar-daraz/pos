(ns pos.app-routes
  (:require [pos.routes :as frontend-routes]
            [clojure.walk :as walk]))

(defn index-html [_]
  {:status  200
   ;;:body "Hello World!!!"
   :body (slurp (clojure.java.io/resource "public/index.html"))})

(def routes
  (walk/postwalk
   (fn [itm] (if (map? itm) index-html itm)) frontend-routes/routes))

(comment

  (walk/postwalk
   (fn [itm] (println itm)) routes)

  (walk/postwalk
   (fn [itm] (if (map? itm) index-html itm)) routes)

  )
