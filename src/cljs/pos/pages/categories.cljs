(ns pos.pages.categories
  (:require [re-frame.core :as rf]
            [fork.re-frame :as fork]
            [reitit.frontend.easy :as rfe]))

(defn category-form [{form-id :form-id
                      on-submit-event-id :on-submit-event-id
                      initial-values :initial-values}]
  [fork/form {:form-id form-id
              :path [:form form-id]
              :keywordize-keys true
              :prevent-default? true
              :clean-on-unmount? true
              :initial-values initial-values
              :on-submit #(rf/dispatch [on-submit-event-id %])}

   (fn [{:keys [values handle-change handle-blur handle-submit form-id errors touched]}]
     [:form  {:id form-id :on-submit handle-submit}
      [:div.mb-3
       [:label.form-label "Category Name"]
       [:input.form-control {:id "name" :name "name" :type "text" :placeholder "Enter Category Name"
                             :value (values :name)
                             :on-change handle-change
                             :on-blur handle-blur}]]
      [:button.btn.btn-primary.mt-3 {:type "submit"} "New Category"]])])

(defn new-category []
  [category-form {:form-id :new-category
                  :on-submit-event-id :pos.db/new-category
                  :initial-values {:name ""}}])

(defn update-category [m]
  (let [id (-> m :path-params :id)
        *category (rf/subscribe [:pos.db/category (js/parseInt id)])]
    (fn [m]
      (let [{:category/keys [name] id :db/id} @*category]
        [category-form {:form-id :update-category
                        :on-submit-event-id :pos.db/update-category
                        :initial-values {:name name :id id}}]))))

(defn root []
  (let [categories @(rf/subscribe [:pos.db/categories])]
    [:div.d-flex.flex-column.p-2
     [:a.btn.btn-primary.align-self-end.mb-2 {:href (rfe/href :pos.core/new-category)} "Add New Category"]
     [:table.table.mt-1
      [:thead
       [:tr
        [:th "Category Name"]
        [:th.action-column "Actions"]]]
      [:tbody
       (for [{id :db/id name :category/name} categories]
         ^{:key id} [:tr
                     [:td name]
                     [:td.action-column
                      [:div.btn-group
                       [:button.btn.btn-primary.btn-primary-ghost
                        {:on-click (fn [_] (rf/dispatch [:pos.db/delete-entity id]))} "Delete"]
                       [:a.btn.btn-primary.btn-primary-ghost
                        {:href (rfe/href :pos.core/update-category {:id id})} "Edit"]]]])]]]))
