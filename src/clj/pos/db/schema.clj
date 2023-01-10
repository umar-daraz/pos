(ns pos.db.schema)

(def order-schema [{:db/ident :order/product
                    :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/one
                    :db/doc "The Product in the Order"}

                   {:db/ident :order/party
                    :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/one
                    :db/doc "The Customer in the Order"}

                   {:db/ident :order/quantity
                    :db/valueType :db.type/long
                    :db/cardinality :db.cardinality/one
                    :db/doc "The Quantity in the Order"}

                   {:db/ident :order/date
                    :db/valueType :db.type/instant
                    :db/cardinality :db.cardinality/one
                    :db/doc "The Date of the Order"}

                   {:db/ident :order/status
                    :db/valueType :db.type/keyword
                    :db/cardinality :db.cardinality/one
                    :db/doc "The Status of the Order"}])

(def party-schema [{:db/ident :party/name
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "The Title/Name of the party"}])

(def category-schema [{:db/ident :category/name
                       :db/valueType :db.type/string
                       :db/cardinality :db.cardinality/one
                       :db/doc "The Title/Name of the product category"}])

(def product-schema [{:db/ident :product/name
                      :db/valueType :db.type/string
                      :db/cardinality :db.cardinality/one
                      :db/doc "The title/name of a product"}

                     {:db/ident :product/color
                      :db/valueType :db.type/string
                      :db/cardinality :db.cardinality/one
                      :db/doc "The color of a product"}

                     {:db/ident :product/price
                      :db/valueType :db.type/long
                      :db/cardinality :db.cardinality/one
                      :db/doc "The price of a product"}

                     {:db/ident :product/category
                      :db/valueType :db.type/ref
                      :db/cardinality :db.cardinality/one
                      :db/doc "The category of the Product"}])

(def account-schema [{:db/ident :account/title
                      :db/valueType :db.type/string
                      :db/cardinality :db.cardinality/one
                      :db/doc "The title/name of a account"}
                     {:db/ident :account/type
                      :db/valueType :db.type/keyword
                      :db/cardinality :db.cardinality/one
                      :db/doc "The Account type"}
                     {:db/ident :account/party
                      :db/valueType :db.type/ref
                      :db/cardinality :db.cardinality/one
                      :db/doc "The Account Party"}])

(def transaction-schema [{:db/ident :transaction/type
                          :db/valueType :db.type/keyword
                          :db/cardinality :db.cardinality/one
                          :db/doc "Transaction type, is it debit or credit"}
                         {:db/ident :transaction/amount
                          :db/valueType :db.type/long
                          :db/cardinality :db.cardinality/one
                          :db/doc "The Account Amount"}
                         {:db/ident :transaction/account
                          :db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one
                          :db/doc "The Account where this transaction is happening"}])
