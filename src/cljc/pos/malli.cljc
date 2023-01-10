(ns pos.malli
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.error :as me]))

(def registry*
  (atom {:string (m/-string-schema)
         :int (m/-int-schema)
         :double (m/-double-schema)
         :maybe (m/-maybe-schema)
         :map (m/-map-schema)
         :any (m/-any-schema)
         :keyword (m/-keyword-schema)
         :enum (m/-enum-schema)}))

(defn register! [type ?schema]
  (swap! registry* assoc type ?schema))

(mr/set-default-registry!
 (mr/mutable-registry registry*))

(register! :eid [:int {:error/message :invalid-eid}])

(register! :form/login [:map
                        [:email  [:string {:min 1}]]
                        [:password [:string {:min 1}]]])

(comment

  (m/explain [:enum :pending :completed :cancel] :pending)

  )
