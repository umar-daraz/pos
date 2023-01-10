(ns pos.pages.orders
  (:require [reitit.frontend.easy :as rfe]
            [re-frame.core :as rf]
            [fork.re-frame :as fork]
            ["date-fns" :as date-fns]))

(defn new-order []
  (let [form-id :new-order]
    [:div
     [fork/form {:form-id form-id
                 :path [:form form-id]
                 :keywordize-keys true
                 :prevent-default? true
                 :validation (fn [values]
                               #_(->  :form/new-product
                                      (m/explain values)
                                      (me/humanize)))
                 :clean-on-unmount? true
                 :initial-values {}
                 :on-submit #(rf/dispatch [:pos.db/new-order %])}

      (fn [{:keys [values handle-change handle-blur handle-submit form-id errors touched]}]
        [:form {:id form-id :on-submit handle-submit}
         [:div.mb-3
          [:label.form-label "Party"]
          [:select.form-control {:id "party" :name "party"
                                 :type "select" :placeholder "Select Party"
                                 :value (values :party)
                                 :on-change handle-change
                                 :on-blur handle-blur}
           [:option "Select ...."]
           (for [{name :party/name id :db/id} @(rf/subscribe [:pos.db/parties])]
             ^{:key id}
             [:option {:value id} name])]]
         [:div.mb-3
          [:label.form-label "Product"]
          [:select.form-control {:id "product" :name "product"
                                 :type "select" :placeholder "Select Product"
                                 :value (values :product)
                                 :on-change handle-change
                                 :on-blur handle-blur}
           [:option "Select ...."]
           (for [{name :product/name id :db/id} @(rf/subscribe [:pos.db/products])]
             ^{:key id}
             [:option {:value id} name])]]
         [:div.mb-3
          [:label.form-label "Quantity"]
          [:input.form-control {:id "quantity"
                                :name "quantity"
                                :type "number"
                                :placeholder "Enter Quantity"
                                :value (values :quantity)
                                :on-change handle-change
                                :on-blur handle-blur}]]
         [:div.mb-3
          [:label.form-label "Date"]
          [:input.form-control {:id "date"
                                :name "date"
                                :type "date"
                                :placeholder "Enter Date"
                                :value (values :date)
                                :on-change handle-change
                                :on-blur handle-blur}]]
         [:button.btn.btn-primary.mt-3 {:type "submit"} "Create Order"]])]]))

(defn root []
  (let [orders @(rf/subscribe [:pos.db/orders])]
    [:div.d-flex.flex-column.p-2
     [:a.btn.btn-primary.align-self-end {:href (rfe/href :pos.core/new-order)} "Invoice"]
     [:table.table.mt-1
      [:thead
       [:tr
        [:th "Product"]
        [:th "Customer"]
        [:th "Quantity"]
        [:th "Date"]
        [:th "Status"]
        [:th.action-column "Actions"]]]
      [:tbody
       (for [{id :db/id {product-name :product/name} :order/product
              {party-name :party/name} :order/party
              quantity :order/quantity date :order/date status :order/status} orders]
         ^{:key id} [:tr
                     [:td product-name]
                     [:td party-name]
                     [:td quantity]
                     [:td (date-fns/format date "MM/dd/yyyy")]
                     [:td [:span.badge status]]
                     [:td.action-column
                      [:div
                       (when (= status :pending)
                         [:button.btn.btn-primary.btn-primary-ghost {:on-click (fn [_]
                                                                                 (rf/dispatch [:pos.db/complete-order id]))} "Complete Order"])
                       [:button.btn.btn-primary.btn-primary-ghost {:on-click (fn [_]
                                                                               (rf/dispatch [:pos.db/delete-entity id]))} "Delete"]]]])]]]))
