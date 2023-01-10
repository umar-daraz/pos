(ns pos.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [reitit.frontend :as rf]
            [re-frame.core :as reframe]
            [pos.routes :as routes]
            [pos.db :as db]
            [pos.malli]
            [pos.pages.login :as login-page]
            [pos.pages.products :as products-page]
            [pos.pages.categories :as categories-page]
            [pos.pages.parties :as parties-page]
            [pos.pages.orders :as orders-page]
            [pos.pages.purchases :as purchases-page]
            [pos.pages.transactions :as transactions-page]
            [reitit.frontend.easy :as rfe]))

(defn not-found-page []
  [:div
   [:h1 "Not Found"]])

(defn nav-bar [view-key]
  [:nav.navbar
   [:ul.navbar-nav
    [:li.nav-item (when (contains? #{::orders} view-key) {:class "active"}) [:a {:href (rfe/href ::orders)} "Orders"]]
    [:li.nav-item (when (contains? #{::products ::new-product ::edit-product} view-key) {:class "active"})
     [:a {:href (rfe/href ::products)} "Products"]]
    [:li.nav-item (when (contains? #{::categories ::new-category ::update-category} view-key) {:class "active"})
     [:a {:href (rfe/href ::categories)} "Categories"]]
    [:li.nav-item (when (contains? #{::parties ::new-party ::update-party} view-key) {:class "active"}) [:a {:href (rfe/href ::parties)} "Parties"]]
    [:li.nav-item (when (contains? #{::purchases} view-key) {:class "active"}) [:a {:href (rfe/href ::purchases)} "Purchases"]]
    [:li.nav-item (when (contains? #{::transactions} view-key) {:class "active"}) [:a {:href (rfe/href ::transactions)} "Transactions"]]]])

(defn current-page []
  (let [match @(reframe/subscribe [:pos.db/match])]
    (if match
      (let [view (:name (:data match))]
        [:div.app-wrapper
         [nav-bar view]
         [:div.page-wrapper
          (condp = view
            ::login [login-page/root]
            ::products [products-page/root]
            ::new-product [products-page/new-product]
            ::edit-product [products-page/edit-product match]
            ::categories [categories-page/root]
            ::new-category [categories-page/new-category]
            ::update-category [categories-page/update-category match]
            ::parties [parties-page/root]
            ::new-party [parties-page/new-party]
            ::update-party [parties-page/update-party match]
            ::orders [orders-page/root]
            ::new-order [orders-page/new-order]
            ::purchases [purchases-page/root]
            ::transactions [transactions-page/root]
            [not-found-page])]])
      [not-found-page])))

(def app-routes
  [""
   ["/purchases"
    [""
     {:name ::purchases
      :view ::purchases
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-purchases]))}]}]]
   ["/orders"
    [""
     {:name ::orders
      :view ::orders
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-orders]))}]}]
    ["/new"
     {:name ::new-order
      :view ::new-order
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-products])
                              (reframe/dispatch [:pos.db/fetch-parties]))}]}]]
   ["/categories"
    [""
     {:name ::categories
      :view ::categories
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-categories]))}]}]
    ["/new"
     {:name ::new-category
      :view ::new-category}]
    ["/:id/edit"
     {:name ::update-category
      :view ::update-category
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-categories]))}]}]]
   ["/products"
    [""
     {:name ::products
      :view ::products
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-products]))}]}]
    ["/new"
     {:name ::new-product
      :view ::new-product
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-categories]))}]}]
    ["/:id/edit"
     {:name ::edit-product
      :view ::edit-product
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-categories])
                              (reframe/dispatch [:pos.db/fetch-products]))}]}]]
   ["/parties"
    [""
     {:name ::parties
      :view ::parties
      :controllers [{:start (fn [_]
                              (reframe/dispatch [:pos.db/fetch-parties]))}]}]
    ["/new" {:name ::new-party
             :view ::new-party}]
    ["/:id/edit" {:name ::update-party
                  :view ::update-party
                  :controllers [{:start (fn [_]
                                          (reframe/dispatch [:pos.db/fetch-parties]))}]}]]
   ["/login" {:name ::login
              :view ::login}]
   ["/transactions" {:name ::transactions
                     :view ::transactions
                     :controllers [{:start (fn [_]
                                             (reframe/dispatch [:pos.db/fetch-transactions]))}]}]])

(defn main []
  (reframe/dispatch [:pos.db/init])
  (rfe/start! (rf/router app-routes)
              (fn [m] (reframe/dispatch [:pos.db/navigate! m]))
              {:use-fragment false})
  (rdom/render [current-page] (.getElementById js/document "root")))

(defn ^:dev/after-load start []
  (main))
