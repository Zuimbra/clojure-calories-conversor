(ns conversor.calorias)

(defn gained? [transaction]
  (contains? transaction :food))

(defn lost? [transaction]
  (contains? transaction :exercise))

(defn transaction-calories [transaction]
  (or (:calories transaction) 0))

(defn sum-calories-by [predicate transactions]
  (reduce + 0
          (map transaction-calories
               (filter predicate transactions))))

(defn sum-gained-calories [transactions]
  (sum-calories-by gained? transactions))

(defn sum-lost-calories [transactions]
  (sum-calories-by lost? transactions))

(defn calculate-balance [transactions]
  (- (sum-gained-calories transactions)
     (sum-lost-calories transactions)))

(defn date-greater-or-equal? [date start-date]
  (not (neg? (compare date start-date))))

(defn date-less-or-equal? [date end-date]
  (not (pos? (compare date end-date))))

(defn in-period? [start-date end-date transaction]
  (let [date (:date transaction)]
    (and date
         (date-greater-or-equal? date start-date)
         (date-less-or-equal? date end-date))))

(defn filter-by-period [start-date end-date transactions]
  (filter (partial in-period? start-date end-date) transactions))

(defn calculate-balance-by-period [start-date end-date transactions]
  (calculate-balance
   (filter-by-period start-date end-date transactions)))