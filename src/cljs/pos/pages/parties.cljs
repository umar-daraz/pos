(ns pos.pages.parties
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]
            [fork.re-frame :as fork]))

(defn party-form [{form-id :form-id
                   on-submit-event-id :on-submit-event-id
                   initial-values :initial-values}]
  [fork/form {:form-id form-id
              :path [:form form-id]
              :keywordize-keys true
              :prevent-default? true
              :validation (fn [values]
                            #_(->  :form/new-product
                                   (m/explain values)
                                   (me/humanize)))
              :clean-on-unmount? true
              :on-submit #(rf/dispatch [on-submit-event-id %])
              :initial-values initial-values}
   (fn [{:keys [values handle-change handle-blur handle-submit form-id errors touched]}]
     [:form  {:id form-id :on-submit handle-submit}
      [:div.mb-3
       [:label.form-label "Party Name"]
       [:input.form-control {:id "name" :name "name" :type "text" :placeholder "Enter Party Name"
                             :value (values :name)
                             :on-change handle-change
                             :on-blur handle-blur}]]
      [:button.btn.btn-primary.mt-3 {:type "submit"} "New Party"]])])

(defn new-party []
  [party-form {:form-id :new-party
               :on-submit-event-id :pos.db/new-party
               :initial-values {:name ""}}])

(defn update-party [m]
  (let [id (-> m :path-params :id)
        {id :db/id name :party/name :as party} @(rf/subscribe [:pos.db/party (js/parseInt id)])]
    [:div
     [party-form {:form-id :update-customer
                  :on-submit-event-id :pos.db/update-party
                  :initial-values {:name name :id id}}]]))

(defn root []
  (let [parties @(rf/subscribe [:pos.db/parties])]
    [:div.d-flex.flex-column.p-2
     [:a.btn.btn-primary.align-self-end.mb-2 {:href (rfe/href :pos.core/new-party) } "Add Party"]
     [:table.table.mt-1
      [:thead
       [:tr
        [:th "Customer Name"]
        [:th.action-column "Actions"]]]
      [:tbody
       (for [{id :db/id name :party/name} parties]
         ^{:key id} [:tr
                     [:td name]
                     [:td.action-column
                      [:div.btn-group
                       [:button.btn.btn-primary.btn-primary-ghost {:on-click (fn [_]
                                                                               (rf/dispatch [:pos.db/delete-entity id]))} "Delete"]
                       [:a.btn.btn-primary.btn-primary-ghost {:href (rfe/href :pos.core/update-party {:id id})} "Edit"]]]])]]]))
