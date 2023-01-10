(ns pos.db.core
  (:require [datomic.client.api :as d]))

(def client (d/client {:server-type :dev-local
                       :system "dev"}))

(def conn (d/connect client {:db-name "pet"}))

(def db (d/db conn))

(defn delete-entity [eid]
  (let [tx (d/transact conn {:tx-data [[:db/retractEntity eid]]})]
    {:success true}))

(defn get-orders
  ([] (get-orders {}))
  ([{:keys [id]}]
   (map first
        (d/q {:query [:find '(pull ?e [* {:order/product [*] :order/party [*]}])
                      :in '$ (cond-> [] id (conj '?e))
                      :where ['?e :order/product]]
              :args [(d/db conn) (cond-> [] id (conj id))]}))))

(defn get-transactions
  ([] (get-transactions {}))
  ([{:keys [id]}]
   (map first
        (d/q {:query [:find '(pull ?e [* {:transaction/account [*]}])
                      :in '$ (cond-> [] id (conj '?e))
                      :where ['?e :transaction/type]]
              :args [(d/db conn) (cond-> [] id (conj id))]}))))

(defn get-products []
  (map first
       (d/q '[:find (pull ?e [* {:product/category [*]}])
              :where [?e :product/name]] (d/db conn))))

(defn get-accounts
  ([] (get-accounts {}))
  ([{:keys [type party-id id]}]
   (map first
        (d/q {:query  {:find ['(pull ?e [* {:account/party [*]}])]
                       :in ['$ (-> [] (cond->
                                          id (conj '?e)
                                          type (conj '?type)
                                          party-id (conj '?party-id)))]
                       :where
                       (-> [['?e :account/type]]
                           (cond->
                               type (conj ['?e :account/type '?type])
                               party-id (conj ['?e :account/party '?party-id])))}
              :args [(d/db conn) (-> [] (cond->
                                            id (conj id)
                                            type (conj type)
                                            party-id (conj party-id)))]}))))

(defn get-categories []
  (map first (d/q '[:find (pull ?e [*])
                    :where [?e :category/name]] (d/db conn))))

(defn get-parties []
  (map first (d/q '[:find (pull ?e [*])
                    :where [?e :party/name]] (d/db conn))))

(defn create-product [p]
  (let [temp-id "temp"
        p (assoc p :db/id temp-id)
        tx-data  [p]
        tx (d/transact conn {:tx-data tx-data})
        product-id (get-in tx [:tempids temp-id])]
    {:tx tx
     :product-id product-id}))

(defn update-product [p]
  (let [tx-data [p]
        tx (d/transact conn {:tx-data tx-data})]
    {:tx tx :success true}))

(defn create-category [itm]
  (let [temp-id "temp"
        itm (assoc itm :db/id temp-id)
        tx-data  [itm]
        tx (d/transact conn {:tx-data tx-data})
        category-id (get-in tx [:tempids temp-id])]
    {:tx tx
     :category-id category-id}))

(defn update-category [p]
  (let [tx-data [p]
        tx (d/transact conn {:tx-data tx-data})]
    {:tx tx :success true}))

(defn create-transaction [itm]
  (let [temp-id "temp"
        itm (assoc itm :db/id temp-id) tx-data  [itm]
        tx (d/transact conn {:tx-data tx-data})
        transaction-id (get-in tx [:tempids temp-id])]
    {:tx tx
     :account-id transaction-id}))

(defn create-account [itm]
  (let [temp-id "temp"
        itm (assoc itm :db/id temp-id) tx-data  [itm]
        tx (d/transact conn {:tx-data tx-data})
        account-id (get-in tx [:tempids temp-id])]
    {:tx tx
     :account-id account-id}))

(defn create-party [itm]
  (let [temp-id "temp"
        itm (assoc itm :db/id temp-id)
        tx-data  [itm]
        tx (d/transact conn {:tx-data tx-data})
        party-id (get-in tx [:tempids temp-id])]
    (let [res (create-account {:account/type :party :account/party party-id})]
      (println "party id " party-id)
      {:tx tx
       :party-id party-id})))

(defn update-party [p]
  (let [tx-data [p]
        tx (d/transact conn {:tx-data tx-data})]
    {:tx tx :success true}))

(defn create-order [itm]
  (let [temp-id "temp"
        itm (assoc itm :db/id temp-id)
        tx-data  [itm]
        tx (d/transact conn {:tx-data tx-data})
        order-id (get-in tx [:tempids temp-id])]
    {:tx tx
     :order-id order-id}))

(defn complete-order [id]
  (let [tx-data [{:db/id id :order/status :completed}]
        tx (d/transact conn {:tx-data tx-data})]
    (let [receivable-account (first (get-accounts {:type :receivable}))
          res (create-transaction {:transaction/account (:db/id receivable-account)
                                   :transaction/amount 10
                                   :transaction/type :debit})]
      (def res res)
      {:tx tx :success true})))

(comment

  (get-accounts {:party-id 101155069755496})

  (get-accounts)

  (d/create-database client {:db-name "pet"})

  (d/delete-database client {:db-name "pet"})

  (d/transact conn {:tx-data pet.db.schema/product-schema})

  (d/transact conn {:tx-data pet.db.schema/category-schema})

  (d/transact conn {:tx-data pet.db.schema/party-schema})

  (d/transact conn {:tx-data pet.db.schema/order-schema})

  (d/transact conn {:tx-data pet.db.schema/account-schema})

  (d/transact conn {:tx-data pet.db.schema/transaction-schema})

  (def first-products [{:product/name "IPhone 14 Pro 64GB"
                        :product/color "Deep Purple"
                        :product/category 79164837199946
                        :product/price 1500}
                       {:product/name "IPhone 14 Pro 256GB"
                        :product/category 79164837199946
                        :product/color "Black"
                        :product/price 2500}])

  (def first-categories [{:category/name "Mobile"}])

  (def first-parties [{:party/name "Mobx"}])

  (def account-types #{:cash :anonymous :receivable :payable :party})

  (def first-accounts [{:account/title "Cash" :account/type :cash}
                       {:account/title "Retail Customer" :account/type :anonymous}
                       {:account/title "Account Receivable" :account/type :receivable}
                       {:account/title "Account Payable" :account/type :payable}])

  (delete-entity 96757023244389)

  (d/transact conn {:tx-data first-parties})

  (d/transact conn {:tx-data first-products})

  (d/transact conn {:tx-data first-categories})

  (d/transact conn {:tx-data first-accounts})

  (def tx (d/transact conn {:tx-data [{:db/id "temp" :product/name "abc" :product/color "Blue" :product/price 10}]})))
