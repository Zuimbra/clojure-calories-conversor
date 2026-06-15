(ns conversor.external
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]))

;; =========================
;; Funcoes auxiliares
;; =========================

(defn parse-json [response]
  (json/parse-string (:body response) true))

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

(defn value-of [m k]
  (or (get m k)
      (get m (name k))))

;; =========================
;; USDA - alimentos
;; =========================

(def usda-url "https://api.nal.usda.gov/fdc/v1/foods/search")

(defn usda-key []
  (System/getenv "USDA_API_KEY"))

(defn food-api-configured? []
  (not (str/blank? (usda-key))))

(defn usda-get [food]
  (if-not (food-api-configured?)
    {:ok false
     :error "USDA_API_KEY nao configurada."}

    (let [response (http/get usda-url
                             {:query-params {"query" food
                                             "api_key" (usda-key)}
                              :throw-exceptions false})]
      (if (= 200 (:status response))
        {:ok true
         :data (parse-json response)}
        {:ok false
         :status (:status response)
         :error (:body response)}))))

(defn energy-nutrient? [nutrient]
  (= (value-of nutrient :nutrientName) "Energy"))

(defn food-energy-per-100g [food-data]
  (let [foods (value-of food-data :foods)
        first-food (first foods)
        nutrients (value-of first-food :foodNutrients)
        energy (some #(when (energy-nutrient? %)
                        (value-of % :value))
                     nutrients)]
    (to-number energy)))

(defn food-calories [food quantity]
  (let [result (usda-get food)]
    (if-not (:ok result)
      result

      (let [calories-per-100g (food-energy-per-100g (:data result))
            total (* (/ calories-per-100g 100.0)
                     (to-number quantity))]
        (if (pos? total)
          {:ok true
           :calories (round-calories total)}
          {:ok false
           :error "Nao foi possivel obter calorias desse alimento."})))))

;; =========================
;; API Ninjas - exercicios
;; =========================

(def api-ninjas-base-url "https://api.api-ninjas.com/v1")

(defn api-ninjas-key []
  (System/getenv "API_NINJAS_KEY"))

(defn exercise-api-configured? []
  (not (str/blank? (api-ninjas-key))))

(defn api-ninjas-get [endpoint params]
  (if-not (exercise-api-configured?)
    {:ok false
     :error "API_NINJAS_KEY nao configurada."}

    (let [response (http/get (str api-ninjas-base-url endpoint)
                             {:headers {"X-Api-Key" (api-ninjas-key)}
                              :query-params params
                              :throw-exceptions false})]
      (if (= 200 (:status response))
        {:ok true
         :data (parse-json response)}
        {:ok false
         :status (:status response)
         :error (:body response)}))))

(defn kg->lb [kg]
  (* (to-number kg) 2.20462))

(defn exercise-calories [exercise duration weight-kg]
  (let [result (api-ninjas-get "/caloriesburned"
                               {:activity exercise
                                :duration duration
                                :weight (kg->lb weight-kg)})]
    (if-not (:ok result)
      result

      (let [first-result (first (:data result))
            total (to-number (value-of first-result :total_calories))]
        (if (pos? total)
          {:ok true
           :calories (round-calories total)}
          {:ok false
           :error "Nao foi possivel obter calorias desse exercicio."})))))