(ns pos.orders)

(defn order-summary [{:order/keys [product quantity]}]
  (*
   (:product/price product)
   quantity))
