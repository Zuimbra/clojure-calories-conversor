(ns conversor.model)

(defn create-user [{:keys [name email age weight height gender]}]
  {:id (java.util.UUID/randomUUID)
   :name name
   :email email
   :age age
   :weight weight
   :height height
   :gender gender})

(defn valid-user? [user]
   (and (not (empty? (:name user)))
        (not (empty? (:email user)))
        (pos? (:age user))
        (pos? (:weight user))
        (pos? (:height user))))

(defn create-food-transaction [{:keys [user food quantity calories date]}]
  {:id (java.util.UUID/randomUUID)
   :user user
   :food food
   :quantity quantity
   :calories calories
   :date date})

(defn create-exercise-transaction [{:keys [user exercise duration calories date]}]
  {:id (java.util.UUID/randomUUID)
   :user user
   :exercise exercise
   :duration duration
   :calories calories
   :date date})

(defn valid-transaction? [transaction]
  (and (not (nil? (:user transaction)))
       (not (nil? (:date transaction)))))