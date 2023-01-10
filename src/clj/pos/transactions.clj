(ns pos.transactions
  (:require [pos.orders :as orders]
            [clojure.core.match :refer [match]]
            [pos.db.core :as db]))

(defn event->transactions [event]
  (match [event]
         [{:status :completed}] :its-completed
         [{:status :incompleted}] :its-not-completed-yet
         :else nil))

(comment

  (def order (first (db/get-orders {:id 79164837199978})))

  (let [{:order/keys [product]} order]
    product)

  (orders/order-summary order)

  order

  )
