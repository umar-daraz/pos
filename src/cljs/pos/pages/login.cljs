(ns pos.pages.login
  (:require [fork.re-frame :as fork]
            [re-frame.core :as rf]
            [malli.core :as m]
            [malli.error :as me]
            [fork.reagent :as fork-reagent]))

(defn root []
  [:div#login.center
   [fork/form {:form-id :login
               :path [:form :login]
               :keywordize-keys true
               :prevent-default? true
               :validation (fn [values]
                             (->  :form/login
                                  (m/explain values)
                                  (me/humanize)))
               :clean-on-unmount? true
               :initial-values {:email "" :password ""}
               :on-submit #(rf/dispatch [:pet.db/login-handler %])}
    (fn [{:keys [values handle-change handle-blur handle-submit form-id errors touched]}]
      [:form.login-form {:id form-id :on-submit handle-submit}
       [:input.form-control {:id "email" :name "email" :type "text" :placeholder "Enter Email"
                             :value (values :email)
                             :on-change handle-change
                             :on-blur handle-blur}]
       (when (touched :email)
         [:div (first (get errors :email))])
       [:input.form-control {:id "password" :name "password"
                             :type "password" :placeholder "Enter Password"
                             :value (values :password)
                             :on-change handle-change
                             :on-blur handle-blur}]
       (when (touched :password)
         [:div (first (get errors :password))])
       [:button.btn.btn-primary {:type "submit"} "Login"]])]])

(comment

  (->  :form/login
       (m/explain {:email "" :password ""})
       (me/humanize))

  )
