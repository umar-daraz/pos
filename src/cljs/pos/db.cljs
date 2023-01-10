(ns pos.db
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [fork.re-frame :as fork]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]
            [day8.re-frame.http-fx]))
;; subs

(rf/reg-sub ::match
  (fn [db] (:match db)))

(rf/reg-sub ::products
  (fn [db] (vals (:products db))))

(rf/reg-sub ::product
  (fn [db [_ id]]
    (get-in db [:products id])))

(rf/reg-sub ::categories
  (fn [db] (vals (:categories db))))

(rf/reg-sub ::category
  (fn [db [_ id]]
    (get-in db [:categories id])))

(rf/reg-sub ::parties
  (fn [db] (vals (:parties db))))

(rf/reg-sub ::party
  (fn [db [_ id]]
    (get-in db [:parties id])))

(rf/reg-sub ::orders
  (fn [db] (vals (:orders db))))

(rf/reg-sub ::transactions
  (fn [db] (vals (:transactions db))))

;; events

(rf/reg-event-db ::init
  (fn [db] {:match nil}))

(rf/reg-event-db ::navigate!
  (fn [db [_ new-match]]
    (update db :match (fn [old-match]
                        (when new-match
                          (assoc new-match
                                 :controllers (rfc/apply-controllers (:controllers old-match) new-match)))))))

(rf/reg-event-fx
    ::login-handler
  (fn [{db :db} [_ {:keys [values path]}]]
    {:db (-> db
             (fork/set-submitting path true)
             (fork/set-server-message path "Login Failed!!!"))
     :http-xhrio {:method :post
                  :uri "/api/account/login"
                  :params values
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)}}))

(rf/reg-event-fx
    ::complete-order
  (fn [{db :db} [_ order-id]]
    {:http-xhrio {:method :post
                  :uri "/api/order/complete"
                  :params {:id order-id}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::complete-order-success]}}))

(rf/reg-event-fx
    ::complete-order-success
  (fn [_ _]
    (rf/dispatch [::fetch-orders])))

(rf/reg-event-fx
    ::new-product
  (fn [{db :db} [_ {{:keys [name price color category] :as vals} :values  path :path}]]
    {:db (-> db
             (fork/set-submitting path true))
     :http-xhrio {:method :post
                  :uri "/api/product"
                  :params {:product/name name :product/price (js/parseInt price)
                           :product/color color
                           :product/category (js/parseInt category)}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::new-product-success]}}))

(rf/reg-event-fx
    ::new-product-success
  (fn [_ _]
    (rfe/replace-state :pos.core/products)))

(rf/reg-event-fx
    ::update-product
  (fn [{db :db} [_ {{:keys [id name price color category] :as values} :values path :path}]]
    {:db (-> db
             (fork/set-submitting path true))
     :http-xhrio {:method :put
                  :uri "/api/product"
                  :params {:product/name name :product/price (js/parseInt price)
                           :product/color color :db/id id
                           :product/category (js/parseInt category)}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::update-product-success]}}))

(rf/reg-event-fx
    ::update-product-success
  (fn [_ _]
    (rfe/replace-state :pos.core/products)))

(rf/reg-event-fx
    ::new-category
  (fn [{db :db} [_ {{:keys [name] :as values} :values path :path}]]
    {:db (-> db
             (fork/set-submitting path true))
     :http-xhrio {:method :post
                  :uri "/api/category"
                  :params {:category/name name}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::new-category-success]}}))

(rf/reg-event-db
    ::new-category-success
  (fn [_ _]
    (rfe/replace-state :pos.core/categories)))

(rf/reg-event-fx
    ::update-category
  (fn [{db :db} [_ {{:keys [id name] :as values} :values path :path}]]
    {:db (-> db
             (fork/set-submitting path true))
     :http-xhrio {:method :put
                  :uri "/api/category"
                  :params {:db/id id :category/name name}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::update-category-success]}}))

(rf/reg-event-db
    ::update-category-success
  (fn [_ _]
    (rfe/replace-state :pos.core/categories)))

(rf/reg-event-fx
    ::delete-entity
  (fn [{db :db} [_ id]]
    {:http-xhrio {:method :delete
                  :uri "/api/delete"
                  :params {:id id}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::new-product-success]}}))

(rf/reg-event-fx
    ::fetch-products
  (fn [{db :db} _]
    {:http-xhrio {:method :get
                  :uri "/api/products"
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::fetch-products-success]}}))

(rf/reg-event-db
    ::fetch-products-success
  (fn [db [_ res]]
    (assoc db :products (into {} (map (juxt :db/id identity) res)))))


(rf/reg-event-fx
    ::fetch-categories
  (fn [{db :db} _]
    {:http-xhrio {:method :get
                  :uri "/api/categories"
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::fetch-categories-success]}}))

(rf/reg-event-db
    ::fetch-categories-success
  (fn [db [_ res]]
    (assoc db :categories (into {} (map (juxt :db/id identity) res)))))


(rf/reg-event-fx
    ::fetch-parties
  (fn [{db :db} _]
    {:http-xhrio {:method :get
                  :uri "/api/parties"
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::fetch-parties-success]}}))

(rf/reg-event-db
    ::fetch-parties-success
  (fn [db [_ res]]
    (assoc db :parties (into {} (map (juxt :db/id identity) res)))))


(rf/reg-event-fx
    ::new-party
  (fn [{db :db} [_ {{:keys [name] :as values} :values path :path}]]
    {:db (-> db
             (fork/set-submitting path true))
     :http-xhrio {:method :post
                  :uri "/api/party"
                  :params {:party/name name}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::new-party-success]}}))

(rf/reg-event-db
    ::new-party-success
  (fn [_ _]
    (rfe/replace-state :pos.core/parties)))

(rf/reg-event-fx
    ::update-party
  (fn [{db :db} [_ {{:keys [id name] :as values} :values path :path}]]
    {:db (-> db
             (fork/set-submitting path true))
     :http-xhrio {:method :put
                  :uri "/api/party"
                  :params {:db/id id :party/name name}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::update-party-success]}}))

(rf/reg-event-db
    ::update-party-success
  (fn [_ _]
    (rfe/replace-state :pos.core/parties)))

(rf/reg-event-fx
    ::new-order
  (fn [{db :db} [_ {{:keys [party product quantity date] :as values} :values path :path}]]
    {:db (-> db
             (fork/set-submitting path true))
     :http-xhrio {:method :post
                  :uri "/api/order"
                  :params {:order/product (js/parseInt product)
                           :order/party (js/parseInt party)
                           :order/quantity (js/parseInt quantity)
                           :order/date (new js/Date date)
                           :order/status :pending}
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::new-order-success]}}))

(rf/reg-event-db
    ::new-order-success
  (fn [_ _]
    (rfe/replace-state :pos.core/orders)))

(rf/reg-event-fx
    ::fetch-orders
  (fn [{db :db} _]
    {:http-xhrio {:method :get
                  :uri "/api/orders"
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::fetch-orders-success]}}))

(rf/reg-event-db
    ::fetch-orders-success
  (fn [db [_ res]]
    (assoc db :orders (into {} (map (juxt :db/id identity) res)))))


(rf/reg-event-fx
    ::fetch-transactions
  (fn [{db :db} _]
    {:http-xhrio {:method :get
                  :uri "/api/transactions"
                  :format (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success [::fetch-transactions-success]}}))

(rf/reg-event-db
    ::fetch-transactions-success
  (fn [db [_ res]]
    (assoc db :transactions (into {} (map (juxt :db/id identity) res)))))

(comment

  (:customers @re-frame.db/app-db)

  (rf/dispatch [::fetch-customers])

  (rf/dispatch [::fetch-products])

  @(rf/subscribe [::products])

  @(rf/subscribe [::product "96757023244369"])

  @(rf/subscribe [::product "96757023244369"])

  (def id "96757023244369")

  (def idd 96757023244369)

  (def id 83562883711057)

  @(rf/subscribe [::customers])

  @(rf/subscribe [::customers id])


  @(rf/subscribe [::product id])

  @(rf/subscribe [::customers])


  (rf/dispatch [::fetch-categories])

  @(rf/subscribe [:pos.db/category 96757023244370])

  (get (:products @re-frame.db/app-db) id)

  (ajax/transit-request-format)
  )
