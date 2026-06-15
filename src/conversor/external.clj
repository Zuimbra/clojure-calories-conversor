(ns conversor.external
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]))

(def base-url "https://api.api-ninjas.com/v1")

(defn api-key []
  (System/getenv "API_NINJAS_KEY"))

(defn configured? []
  (not (str/blank? (api-key))))

(defn parse-json [response]
  (json/parse-string (:body response) true))

(defn get-api [endpoint params]
  (if-not (configured?)
    {:ok false
     :error "API_NINJAS_KEY nao configurada."}

    (let [response (http/get (str base-url endpoint)
                             {:headers {"X-Api-Key" (api-key)}
                              :query-params params
                              :throw-exceptions false})]
      (if (= 200 (:status response))
        {:ok true
         :data (parse-json response)}
        {:ok false
         :status (:status response)
         :error (:body response)}))))

(defn to-number [value]
  (try
    (cond
      (number? value) value
      (string? value) (Double/parseDouble value)
      :else 0)
    (catch Exception _
      0)))

(defn round-calories [value]
  (long (Math/round (double value))))

(defn food-calories [food quantity]
  (let [query (str quantity " " food)
        result (get-api "/nutrition" {:query query})]
    (if-not (:ok result)
      result

      (let [items (:data result)
            calories-list (map #(to-number (:calories %)) items)
            total (reduce + 0 calories-list)]
        (if (pos? total)
          {:ok true
           :calories (round-calories total)}
          {:ok false
           :error "Nao foi possivel obter calorias desse alimento."})))))

(defn kg->lb [kg]
  (* kg 2.20462))

(defn exercise-calories [exercise duration weight-kg]
  (let [result (get-api "/caloriesburned"
                        {:activity exercise
                         :duration duration
                         :weight (kg->lb weight-kg)})]
    (if-not (:ok result)
      result

      (let [first-result (first (:data result))
            total (to-number (:total_calories first-result))]
        (if (pos? total)
          {:ok true
           :calories (round-calories total)}
          {:ok false
           :error "Nao foi possivel obter calorias desse exercicio."})))))