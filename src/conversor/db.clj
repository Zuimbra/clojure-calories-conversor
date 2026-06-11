(ns conversor.db)

(defonce user-db (atom nil))

(defonce transaction-db (atom '()))

(defn save-user! [user]
  (reset! user-db user) 
  user)

(defn get-user []
  @user-db)

(defn add-transaction! [transaction]
   (swap! transaction-db conj transaction)
  transaction)

(defn get-transactions []
   @transaction-db)

(defn reset-db! []
   (reset! user-db nil)
   (reset! transaction-db nil)
   {:message "Database reset."})