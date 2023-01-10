(ns pos.pages.products
  (:require [re-frame.core :as rf]
            [fork.re-frame :as fork]
            [malli.core :as m]
            [malli.error :as me]
            [reitit.frontend.easy :as rfe]))

(defn product-form [{form-id :form-id
                     on-submit-event-id :on-submit-event-id
                     initial-values :initial-values}]
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
               :initial-values initial-values
               :on-submit #(rf/dispatch [on-submit-event-id %])}
    (fn [{:keys [values handle-change handle-blur handle-submit form-id errors touched]}]
      [:form {:id form-id :on-submit handle-submit}
       [:div.mb-3
        [:label.form-label "Product Name"]
        [:input.form-control {:id "name" :name "name" :type "text" :placeholder "Enter Product Name"
                              :value (values :name)
                              :on-change handle-change
                              :on-blur handle-blur}]]
       [:div.mb-3
        [:label.form-label "Product Price"]
        [:input.form-control {:id "price" :name "price"
                              :type "number" :placeholder "Enter Price"
                              :value (values :price)
                              :on-change handle-change
                              :on-blur handle-blur}]
        (when (touched :password)
          [:div (first (get errors :password))])]
       [:div.mb-3
        [:label.form-label "Product Color"]
        [:input.form-control {:id "color" :name "color"
                              :type "text" :placeholder "Enter Color"
                              :value (values :color)
                              :on-change handle-change
                              :on-blur handle-blur}]]
       [:div.mb-3
        [:label.form-label "Product Category"]
        [:select.form-control {:id "category" :name "category"
                               :type "text" :placeholder "Select Category"
                               :value (values :category)
                               :on-change handle-change
                               :on-blur handle-blur}
         (for [{name :category/name id :db/id} @(rf/subscribe [:pos.db/categories])]
           ^{:key id}
           [:option {:value id} name])]]
       [:button.btn.btn-primary.mt-3 {:type "submit"} "New Product"]])]])

(defn new-product []
  [product-form {:form-id :new-product
                 :on-submit-event-id :pos.db/new-product
                 :initial-values {:name "" :price 0 :color ""}}])

(defn edit-product [m]
  (let [id (-> m :path-params :id)
        *product (rf/subscribe [:pos.db/product  (js/parseInt id)])
        *products (rf/subscribe [:pos.db/products])]
    (fn [m]
      (let [{:product/keys [name price color category]} @*product]
        (def category category)
        [product-form {:form-id :edit-product
                       :on-submit-event-id :pos.db/update-product
                       :initial-values {:name name :price price
                                        :category (:db/id category)
                                        :color color :id (:db/id @*product)}}]))))

(defn root []
  (let [products @(rf/subscribe [:pos.db/products])]
    [:div.d-flex.flex-column.p-2
     [:a.btn.btn-primary.align-self-end.mb-2 {:href (rfe/href :pos.core/new-product)} "Add New Product"]
     [:table.table.mt-1
      [:thead
       [:tr
        [:th "Product Name"]
        [:th "Color "]
        [:th "Price"]
        [:th "Category"]
        [:th.action-column "Actions"]]]
      [:tbody
       (for [{id :db/id name :product/name color :product/color price :product/price {category :category/name} :product/category} products]
         ^{:key id} [:tr
                     [:td name]
                     [:td color]
                     [:td price]
                     [:td category]
                     [:td.action-column
                      [:div
                       [:button.btn.btn-primary.btn-primary-ghost {:on-click (fn [_]
                                                                               (rf/dispatch [:pos.db/delete-entity id]))} "Delete"]
                       [:a.btn.btn-primary.btn-primary-ghost {:href (rfe/href :pos.core/edit-product  {:id id}) } "Edit"]]]])]]]))

(comment

  (rfe/href :pos.core/edit-product  {:id 1})

  )
