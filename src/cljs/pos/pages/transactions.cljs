(ns pos.pages.transactions
  (:require [re-frame.core :as rf]))

;; [:td (date-fns/format date "MM/dd/yyyy")]

(defn root []
  (let [transactions @(rf/subscribe [:pos.db/transactions])]
    [:div.d-flex.flex-column.p-2
     [:table.table.mt-1
      [:thead
       [:tr
        [:th "Account Title"]
        [:th "Transaction Type"]
        [:th "Transaction Amount"]]]
      [:tbody
       (for [{id :db/id {account-title :account/title} :transaction/account
              type :transaction/type
              amount :transaction/amount} transactions]
         ^{:key id} [:tr
                     [:td account-title]
                     [:td type]
                     [:td amount]])]]]))
