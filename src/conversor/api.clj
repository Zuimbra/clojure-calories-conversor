(ns conversor.api
  (:require [cheshire.core :as json]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [conversor.model :as model]
            [conversor.db :as db]
            [conversor.calorias :as calorias]))

(defn json-response
  ([data]
   (json-response 200 data))
  ([status data]
   {:status status
    :headers {"Content-Type" "application/json; charset=utf-8"}
    :body (json/generate-string data)}))

(defn read-json-body [request]
  (try
    (json/parse-string (slurp (:body request)) true)
    (catch Exception _
      {})))

(defn valid-calories? [transaction]
  (number? (:calories transaction)))

(defn register-user-handler [request]
  (let [body (read-json-body request)
        user (model/create-user body)]
    (if (model/valid-user? user)
      (do
        (db/save-user! user)
        (json-response {:message "User registered successfully."
                        :user user}))
      (json-response 400 {:error "Invalid user data."}))))

(defn get-user-handler [_request]
  (let [user (db/get-user)]
    (if user
      (json-response {:user user})
      (json-response 404 {:error "No user registered."}))))

(defn register-food-handler [request]
  (let [body (read-json-body request)
        user (db/get-user)
        transaction (model/create-food-transaction
                     (assoc body :user user))]
    (cond
      (nil? user)
      (json-response 400 {:error "Register a user before adding food transactions."})

      (not (model/valid-transaction? transaction))
      (json-response 400 {:error "Invalid food transaction."})

      (not (valid-calories? transaction))
      (json-response 400 {:error "Calories must be a number."})

      :else
      (do
        (db/add-transaction! transaction)
        (json-response {:message "Food transaction registered successfully."
                        :transaction transaction})))))

(defn register-exercise-handler [request]
  (let [body (read-json-body request)
        user (db/get-user)
        transaction (model/create-exercise-transaction
                     (assoc body :user user))]
    (cond
      (nil? user)
      (json-response 400 {:error "Register a user before adding exercise transactions."})

      (not (model/valid-transaction? transaction))
      (json-response 400 {:error "Invalid exercise transaction."})

      (not (valid-calories? transaction))
      (json-response 400 {:error "Calories must be a number."})

      :else
      (do
        (db/add-transaction! transaction)
        (json-response {:message "Exercise transaction registered successfully."
                        :transaction transaction})))))

(defn get-period-transactions [params]
  (let [start-date (:start params)
        end-date (:end params)
        transactions (db/get-transactions)]
    (if (and start-date end-date)
      (calorias/filter-by-period start-date end-date transactions)
      transactions)))

(defn extract-handler [request]
  (let [transactions (get-period-transactions (:params request))]
    (json-response {:transactions transactions})))

(defn balance-handler [request]
  (let [transactions (get-period-transactions (:params request))]
    (json-response {:gained-calories (calorias/sum-gained-calories transactions)
                    :lost-calories (calorias/sum-lost-calories transactions)
                    :balance (calorias/calculate-balance transactions)})))

(defn reset-handler [_request]
  (json-response (db/reset-db!)))

(defroutes routes
  (POST "/user" request (register-user-handler request))
  (GET "/user" request (get-user-handler request))

  (POST "/foods" request (register-food-handler request))
  (POST "/exercises" request (register-exercise-handler request))

  (GET "/extract" request (extract-handler request))
  (GET "/balance" request (balance-handler request))

  (POST "/reset" request (reset-handler request))

  (route/not-found
   (json/generate-string {:error "Route not found."})))

(def app
  (wrap-params routes))

(defn start-server [port]
  (run-jetty app {:port port
                  :join? true}))