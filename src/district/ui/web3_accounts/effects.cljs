(ns district.ui.web3-accounts.effects
  (:require
    [re-frame.core :refer [reg-fx dispatch]]))

(reg-fx
  ::watch-accounts
  (fn [{:keys [:on-change]}]
    (when (some-> js/window (aget "ethereum") (aget "on"))
      (js-invoke
        (aget js/window "ethereum")
        "on"
        "accountsChanged"
        (fn [accounts]
          (dispatch (conj on-change (js->clj accounts))))))))

