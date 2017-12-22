(ns district.ui.web3-accounts.queries)

(defn accounts [db]
  (-> db :district.ui.web3-accounts :accounts))

(defn assoc-accounts [db accounts]
  (assoc-in db [:district.ui.web3-accounts :accounts] accounts))

(defn active-account [db]
  (-> db :district.ui.web3-accounts :active-account))

(defn assoc-active-account [db active-account]
  (assoc-in db [:district.ui.web3-accounts :active-account] active-account))

(defn dissoc-web3-accounts [db]
  (dissoc db :district.ui.web3-accounts))
