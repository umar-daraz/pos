(ns pos.pages.purchases
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

(defn root []
  (let [orders @(rf/subscribe [:pos.db/orders])]
    [:div.d-flex.flex-column.p-2
     [:a.btn.btn-primary.align-self-end {:href (rfe/href :pos.core/new-order)} "Purchase"]
     [:table.table.mt-1
      [:thead
       [:tr
        [:th "Product"]
        [:th "Vendor"]
        [:th "Quantity"]
        [:th "Actions"]]]
      [:tbody
       (for [{id :db/id {product-name :product/name} :order/product {customer-name :customer/name} :order/customer quantity :order/quantity} orders]
         ^{:key id} [:tr
                     [:td product-name]
                     [:td customer-name]
                     [:td quantity]
                     [:td.action-column
                      [:div
                       [:button.btn.btn-primary.btn-primary-ghost
                        {:on-click (fn [_] (rf/dispatch [:pos.db/delete-entity id]))}
                        "Delete"]
                       [:a.btn.btn-primary.btn-primary-ghost
                        {:href (rfe/href :pos.core/update-customer {:id id})}
                        "Edit"]]]])]]]))
