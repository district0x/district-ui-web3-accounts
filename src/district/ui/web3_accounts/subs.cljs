(ns district.ui.web3-accounts.subs
  (:require
    [district.ui.web3-accounts.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::accounts
  queries/accounts)

(reg-sub
  ::active-account
  queries/active-account)