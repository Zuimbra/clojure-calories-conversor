(ns conversor.state)

(defonce user-db (atom nil))
(defonce transactions-db (atom '()))

(defn create-user [data]
  {:id (java.util.UUID/randomUUID)
   :name (:name data)
   :email (:email data)
   :age (:age data)
   :weight (:weight data)
   :height (:height data)
   :gender (:gender data)})

(defn valid-user? [user]
  (and (not-empty (:name user))
       (not-empty (:email user))
       (number? (:age user))
       (pos? (:age user))
       (number? (:weight user))
       (pos? (:weight user))
       (number? (:height user))
       (pos? (:height user))))

(defn save-user! [user]
  (reset! user-db user)
  user)

(defn get-user []
  @user-db)

(defn food-transaction [food quantity calories date]
  {:id (java.util.UUID/randomUUID)
   :kind :food
   :food food
   :quantity quantity
   :calories calories
   :date date})

(defn exercise-transaction [exercise duration calories date]
  {:id (java.util.UUID/randomUUID)
   :kind :exercise
   :exercise exercise
   :duration duration
   :calories calories
   :date date})

(defn add-transaction! [transaction]
  (swap! transactions-db conj transaction)
  transaction)

(defn all-transactions []
  @transactions-db)

(defn food? [transaction]
  (= (:kind transaction) :food))

(defn exercise? [transaction]
  (= (:kind transaction) :exercise))

(defn calories-of [transaction]
  (or (:calories transaction) 0))

(defn sum-by [predicate transactions]
  (reduce + 0
          (map calories-of
               (filter predicate transactions))))

(defn gained-calories [transactions]
  (sum-by food? transactions))

(defn lost-calories [transactions]
  (sum-by exercise? transactions))

(defn balance [transactions]
  (- (gained-calories transactions)
     (lost-calories transactions)))

(defn in-period? [start end transaction]
  (let [date (:date transaction)]
    (and (string? date)
         (not (neg? (compare date start)))
         (not (pos? (compare date end))))))

(defn transactions-in-period [start end transactions]
  (filter #(in-period? start end %) transactions))

(defn summary [transactions]
  {:gained (gained-calories transactions)
   :lost (lost-calories transactions)
   :balance (balance transactions)})

(defn reset-db! []
  (reset! user-db nil)
  (reset! transactions-db '())
  {:message "Database reset."})