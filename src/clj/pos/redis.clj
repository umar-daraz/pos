(ns pos.redis
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {} :spec {:uri "redis://localhost:6379/"}})

(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(wcar* (car/ping))

(wcar* (car/set "foo" "bar"))

(wcar* (car/get "foo"))


(wcar* (car/set "order:1" {:a 1 :b 2 :c 3}))

(wcar* (car/get "order:1"))
