(ns frontend.cli
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]))

(def api-url "http://localhost:3000")

(defn parse-response [response]
  (try
    (json/parse-string (:body response) true)
    (catch Exception _
      {:error "Nao foi possivel interpretar a resposta da API."})))

(defn get-request [endpoint]
  (parse-response
   (http/get (str api-url endpoint)
             {:throw-exceptions false})))

(defn post-request [endpoint data]
  (parse-response
   (http/post (str api-url endpoint)
              {:headers {"Content-Type" "application/json"}
               :body (json/generate-string data)
               :throw-exceptions false})))

(defn read-input [message]
  (print message)
  (flush)
  (read-line))

(defn read-int [message]
  (Integer/parseInt (read-input message)))

(defn read-double [message]
  (Double/parseDouble (read-input message)))

(defn print-response [response]
  (println "\nResposta:")
  (println response))

(defn register-user []
  (let [name (read-input "Nome: ")
        email (read-input "Email: ")
        age (read-int "Idade: ")
        weight (read-double "Peso em kg: ")
        height (read-double "Altura em metros: ")
        gender (read-input "Genero: ")]
    (print-response
     (post-request "/user"
                   {:name name
                    :email email
                    :age age
                    :weight weight
                    :height height
                    :gender gender}))))

(defn show-user []
  (print-response
   (get-request "/user")))

(defn register-food []
  (let [food (read-input "Alimento em ingles: ")
        quantity (read-double "Quantidade em gramas: ")
        date (read-input "Data YYYY-MM-DD: ")]
    (print-response
     (post-request "/foods"
                   {:food food
                    :quantity quantity
                    :date date}))))

(defn register-exercise []
  (let [exercise (read-input "Exercicio em ingles: ")
        duration (read-int "Duracao em minutos: ")
        date (read-input "Data YYYY-MM-DD: ")]
    (print-response
     (post-request "/exercises"
                   {:exercise exercise
                    :duration duration
                    :date date}))))

(defn period-endpoint [endpoint]
  (let [start (read-input "Data inicial YYYY-MM-DD ou vazio: ")
        end (read-input "Data final YYYY-MM-DD ou vazio: ")]
    (if (and (not (str/blank? start))
             (not (str/blank? end)))
      (str endpoint "?start=" start "&end=" end)
      endpoint)))

(defn show-extract []
  (print-response
   (get-request (period-endpoint "/extract"))))

(defn show-balance []
  (print-response
   (get-request (period-endpoint "/balance"))))

(defn reset-data []
  (print-response
   (post-request "/reset" {})))

(defn show-menu []
  (println "\n===== Calculadora de Calorias =====")
  (println "1 - Cadastrar usuario")
  (println "2 - Consultar usuario")
  (println "3 - Registrar alimento")
  (println "4 - Registrar exercicio")
  (println "5 - Consultar extrato")
  (println "6 - Consultar saldo")
  (println "7 - Resetar dados")
  (println "0 - Sair"))

(defn handle-option [option]
  (case option
    "1" (register-user)
    "2" (show-user)
    "3" (register-food)
    "4" (register-exercise)
    "5" (show-extract)
    "6" (show-balance)
    "7" (reset-data)
    "0" (println "Encerrando.")
    (println "Opcao invalida.")))

(defn run-frontend []
  (show-menu)
  (let [option (read-input "Escolha uma opcao: ")]
    (handle-option option)
    (when (not= option "0")
      (recur))))

(defn -main [& args]
  (run-frontend))