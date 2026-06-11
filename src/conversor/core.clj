(ns conversor.core
  (:require [conversor.model :as model]
            [conversor.db :as db]
            [conversor.calorias :as calorias])
  (:gen-class))

(defn save-valid-user! [user]
  (when (model/valid-user? user)
    (db/save-user! user)))

(defn save-valid-transaction! [transaction]
  (when (model/valid-transaction? transaction)
    (db/add-transaction! transaction)))

(defn print-user []
  (println "\nUsuário cadastrado:")
  (println (db/get-user)))

(defn print-transactions []
  (println "\nTransações cadastradas:")
  (println (db/get-transactions)))

(defn print-calorie-summary [transactions]
  (println "\nCalorias ganhas:")
  (println (calorias/sum-gained-calories transactions))

  (println "\nCalorias perdidas:")
  (println (calorias/sum-lost-calories transactions))

  (println "\nSaldo calórico:")
  (println (calorias/calculate-balance transactions)))

(defn print-period-summary [start-date end-date transactions]
  (let [period-transactions (calorias/filter-by-period start-date end-date transactions)]
    (println "\nTransações no período:")
    (println period-transactions)

    (println "\nSaldo no período:")
    (println (calorias/calculate-balance period-transactions))))

(defn run-demo []
  (db/reset-db!)

  (let [user (model/create-user {:name "Mateus"
                                 :email "mateus@email.com"
                                 :age 22
                                 :weight 80
                                 :height 1.75
                                 :gender "M"})

        food-transaction (model/create-food-transaction {:user user
                                                         :food "banana"
                                                         :quantity 2
                                                         :calories 180
                                                         :date "2026-06-02"})

        exercise-transaction (model/create-exercise-transaction {:user user
                                                                 :exercise "corrida"
                                                                 :duration 30
                                                                 :calories 250
                                                                 :date "2026-06-02"})]

    (save-valid-user! user)
    (save-valid-transaction! food-transaction)
    (save-valid-transaction! exercise-transaction)

    (print-user)
    (print-transactions)

    (let [transactions (db/get-transactions)]
      (print-calorie-summary transactions)
      (print-period-summary "2026-06-01" "2026-06-30" transactions))))

(defn -main [& args]
  (run-demo))