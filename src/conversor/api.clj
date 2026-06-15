(ns conversor.api
  (:require [cheshire.core :as json]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.string :as str]
            [conversor.state :as state]
            [conversor.external :as external]))

(defn json-response
  ([data]
   (json-response 200 data))
  ([status data]
   {:status status
    :headers {"Content-Type" "application/json; charset=utf-8"}
    :body (json/generate-string data)}))

(defn read-json [request]
  (try
    (json/parse-string (slurp (:body request)) true)
    (catch Exception _
      {})))

(defn param-value [params key-name]
  (let [value (or (get params key-name)
                  (get params (keyword key-name)))]
    (when value
      (clojure.string/trim value))))

(defn get-transactions-from-request [request]
  (let [params (:params request)
        start (param-value params "start")
        end (param-value params "end")
        transactions (state/all-transactions)]
    (if (and start
             end
             (not (clojure.string/blank? start))
             (not (clojure.string/blank? end)))
      (state/transactions-in-period start end transactions)
      transactions)))

(defn register-user [request]
  (let [body (read-json request)
        user (state/create-user body)]
    (if (state/valid-user? user)
      (json-response {:message "User registered successfully."
                      :user (state/save-user! user)})
      (json-response 400 {:error "Invalid user data."}))))

(defn show-user [_request]
  (if-let [user (state/get-user)]
    (json-response {:user user})
    (json-response 404 {:error "No user registered."})))

(defn register-food [request]
  (let [body (read-json request)
        user (state/get-user)
        food (:food body)
        quantity (:quantity body)
        date (:date body)]
    (cond
      (nil? user)
      (json-response 400 {:error "Register a user first."})

      (or (nil? food) (nil? quantity) (nil? date))
      (json-response 400 {:error "Food, quantity and date are required."})

      :else
      (let [api-result (external/food-calories food quantity)]
        (if-not (:ok api-result)
          (json-response 502 {:error (:error api-result)})

          (let [transaction (state/food-transaction food
                                                    quantity
                                                    (:calories api-result)
                                                    date)]
            (json-response {:message "Food transaction registered successfully."
                            :transaction (state/add-transaction! transaction)})))))))

(defn register-exercise [request]
  (let [body (read-json request)
        user (state/get-user)
        exercise (:exercise body)
        duration (:duration body)
        date (:date body)]
    (cond
      (nil? user)
      (json-response 400 {:error "Register a user first."})

      (or (nil? exercise) (nil? duration) (nil? date))
      (json-response 400 {:error "Exercise, duration and date are required."})

      :else
      (let [api-result (external/exercise-calories exercise
                                                   duration
                                                   (:weight user))]
        (if-not (:ok api-result)
          (json-response 502 {:error (:error api-result)})

          (let [transaction (state/exercise-transaction exercise
                                                        duration
                                                        (:calories api-result)
                                                        date)]
            (json-response {:message "Exercise transaction registered successfully."
                            :transaction (state/add-transaction! transaction)})))))))

(defn show-extract [request]
  (json-response
   {:transactions (get-transactions-from-request request)}))

(defn show-balance [request]
  (json-response
   (state/summary
    (get-transactions-from-request request))))

(defn reset-data [_request]
  (json-response
   (state/reset-db!)))

(defroutes routes
  (POST "/user" request (register-user request))
  (GET "/user" request (show-user request))

  (POST "/foods" request (register-food request))
  (POST "/exercises" request (register-exercise request))

  (GET "/extract" request (show-extract request))
  (GET "/balance" request (show-balance request))

  (POST "/reset" request (reset-data request))

  (route/not-found
   (json/generate-string {:error "Route not found."})))

(def app
  (wrap-params routes))

(defn start-server []
  (run-jetty app {:port 3000
                  :join? true}))