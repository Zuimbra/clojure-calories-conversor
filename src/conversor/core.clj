(ns conversor.core
  (:require [conversor.api :as api])
  (:gen-class))

(defn -main [& args]
  (println "Server running at http://localhost:3000")
  (api/start-server))