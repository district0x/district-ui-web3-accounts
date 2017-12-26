(ns district.ui.web3-accounts
  (:require
    [cljs.spec.alpha :as s]
    [district.ui.web3-accounts.events :as events]
    [district.ui.web3]
    [district.ui.window-focus]
    [mount.core :as mount :refer [defstate]]
    [re-frame.core :refer [dispatch-sync]]))

(declare start)
(declare stop)
(defstate web3-accounts
  :start (start (:web3-accounts (mount/args)))
  :stop (stop))

(s/def ::disable-loading-at-start? boolean?)
(s/def ::disable-polling? boolean?)
(s/def ::polling-interval-ms number?)
(s/def ::load-injected-accounts-only? boolean?)
(s/def ::opts (s/nilable (s/keys :opt-un [::disable-polling? ::polling-interval-ms ::load-injected-accounts-only?
                                          ::disable-loading-at-start?])))


(defn start [opts]
  (s/assert ::opts opts)
  (dispatch-sync [::events/start opts])
  opts)


(defn stop []
  (dispatch-sync [::events/stop @web3-accounts]))
