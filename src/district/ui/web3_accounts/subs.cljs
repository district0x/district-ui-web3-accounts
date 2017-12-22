(ns district.ui.web3-accounts.subs
  (:require
    [district.ui.web3-accounts.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::accounts
  queries/accounts)

(reg-sub
  ::has-accounts?
  queries/has-accounts?)

(reg-sub
  ::active-account
  queries/active-account)

(reg-sub
  ::has-active-account?
  queries/has-active-account?)