(ns pos.core
  (:require [reitit.core :as r]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as malli]
            [reitit.ring :as ring]
            [reitit.coercion :as rcc]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.coercion :as coercion]
            [ring.middleware.params :as params]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.util.response :as response]
            [pos.auth :as auth]
            [ring.adapter.jetty :as jetty]
            [pos.app-routes :as frontend-routes]
            [pos.malli]
            [pos.db.core :as db]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(defn delete-entity-handler [{{id :id} :body-params}]
  (let [ret (db/delete-entity id)]
    (response/response ret)))

(defn get-transactions-handler [_]
  (let [transactions (db/get-transactions)]
    (response/response transactions)))

(defn get-products-handler [_]
  (let [products (db/get-products)]
    (response/response products)))

(defn create-product-handler [{payload :body-params :as req}]
  (let [res (db/create-product payload)]
    (response/response (dissoc res :tx))))

(defn update-product-handler [{payload :body-params :as req}]
  (let [res (db/update-product payload)]
    (response/response (dissoc res :tx))))

(defn create-category-handler [{payload :body-params :as req}]
  (let [res (db/create-category payload)]
    (response/response (dissoc res :tx))))

(defn update-category-handler [{payload :body-params :as req}]
  (let [res (db/update-category payload)]
    (response/response (dissoc res :tx))))


(defn create-party-handler [{payload :body-params :as req}]
  (let [res (db/create-party payload)]
    (response/response (dissoc res :tx))))

(defn update-party-handler [{payload :body-params :as req}]
  (let [res (db/update-party payload)]
    (response/response (dissoc res :tx))))

(defn get-categories-handler [_]
  (let [categories (db/get-categories)]
    (def categories categories)
    (response/response categories)))

(defn get-parties-handler [_]
  (let [parties (db/get-parties)]
    (response/response parties)))

(defn get-orders-handler [_]
  (let [orders (db/get-orders)]
    (response/response orders)))

(defn create-order-handler [{payload :body-params :as req}]
  (let [res (db/create-order payload)]
    (response/response (dissoc res :tx))))

(defn complete-order-handler [{{id :id} :body-params}]
  (let [res (db/complete-order id)]
    (response/response (dissoc res :tx))))

(def api-routes ["/api"
                 ["/delete" {:parameters {:body [:map [:id :eid]]}
                             :delete {:handler delete-entity-handler}}]
                 ["/transactions" {:get get-transactions-handler}]
                 ["/orders" {:get get-orders-handler}]
                 ["/order" {:post {:parameters {:body [:map
                                                       [:order/product :eid]
                                                       [:order/party :eid]
                                                       [:order/quantity :int]
                                                       [:order/date :any]
                                                       [:order/status [:enum :pending :completed :cancel]]]}
                                   :handler create-order-handler}}]
                 ["/order/complete" {:post {:parameters {:body
                                                         [:map [:id :eid]]}}
                                     :handler complete-order-handler}]
                 ["/parties" {:get {:handler get-parties-handler}}]
                 ["/party" {:post {:parameters {:body [:map [:party/name :string]]}
                                   :handler create-party-handler}
                            :put {:parameters {:body [:map
                                                      [:party/name :string]
                                                      [:db/id :eid]]}
                                  :handler update-party-handler}}]
                 ["/products" {:get {:handler get-products-handler}}]
                 ["/product" {:post {:parameters {:body [:map
                                                         [:product/name :string]
                                                         [:product/color :string]
                                                         [:product/category :eid]
                                                         [:product/price :int]]}
                                     :handler create-product-handler}
                              :put {:parameters {:body [:map
                                                        [:db/id :eid]
                                                        [:product/category :eid]
                                                        [:product/name :string]
                                                        [:product/color :string]
                                                        [:product/price :int]]}
                                    :handler update-product-handler}}]
                 ["/categories" {:get {:handler get-categories-handler}}]
                 ["/category" {:post {:parameters {:body [:map
                                                          [:category/name :string]]}
                                      :handler create-category-handler}
                               :put {:parameters {:body [:map
                                                         [:category/name :string]
                                                         [:db/id :eid]]}
                                     :handler update-category-handler}}]
                 ["/account" ["/login" {:post {#_#_:parameters {:body :form/login}
                                               :handler (fn [{body-params :body-params :as req}]
                                                          (def req req)
                                                          {:status 200
                                                           :body body-params})}}]]
                 ["/ping/:id" {:get {:parameters {:path [:map
                                                         [:id :string]]}
                                     :handler (fn [{path-params :path-params}]
                                                {:status 200
                                                 :body path-params})}}]])
(defn index-html [_]
  {:status  200
   ;;:body "Hello World!!!"
   :body (slurp (clojure.java.io/resource "public/index.html"))})

(def handler
  (ring/ring-handler
   (ring/router
    [""
     [api-routes]
     ["/js/*" (ring/create-resource-handler {:path "/"})]
     ["/css/*" (ring/create-resource-handler {:path ""})]
     ["/webfonts/*" (ring/create-resource-handler {:path ""})]
     ["/bootstrap-utilities.css" (ring/create-resource-handler {:path ""})]
     ["/**" index-html]]
    {:conflicts nil
     :data {:coercion malli/coercion
            :muuntaja (m/create (->
                                 m/default-options
                                 (assoc :default-format "application/transit+json")))
            :middleware [parameters/parameters-middleware ;; query-params & form-params
                         muuntaja/format-negotiate-middleware ;; content-negotiation
                         muuntaja/format-response-middleware ;; encoding response body
                         coercion/coerce-exceptions-middleware ;; exception handling
                         muuntaja/format-request-middleware ;; decoding request body
                         coercion/coerce-response-middleware ;; coercing response bodys
                         coercion/coerce-request-middleware ;; coercing request parameters
                         multipart/multipart-middleware]}})
   (ring/routes
    (ring/create-default-handler))))

(def app (-> handler
             (wrap-authentication auth/basic-backend)
             (wrap-authorization auth/basic-backend)))

(defn start! []
  (jetty/run-jetty #'app {:port 3002 :join? false}))

(comment

  req

  (def server (start!))

  server

  (.stop server)

  (-> (app {:request-method :get
            :uri "/api/ping/1"})
      :body
      slurp
      clojure.edn/read-string)

  (-> (app {:request-method :post
            :body-params {:product/name "abc" :product/color "abc" :product/price 1}
            :uri "/api/product"})
      :body
      slurp
      clojure.edn/read-string)

  )
