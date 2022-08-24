(ns district.ui.web3-accounts.events
  (:require [cljs-web3-next.core :as web3]
            [cljs-web3-next.eth :as web3-eth]
            [cljs.spec.alpha :as s]
            [day8.re-frame.forward-events-fx]
            [district.ui.web3-accounts.effects :as effects]
            [district.ui.web3-accounts.queries :as queries]
            [district.ui.web3.events :as web3-events]
            [district.ui.web3.queries :as web3-queries]
            [district.ui.window-focus.queries :as window-focus-queries]
            [district0x.re-frame.interval-fx]
            [district0x.re-frame.spec-interceptors :as spec-interceptors]
            [district0x.re-frame.web3-fx]
            [eip55.core :as eip55]
            [re-frame.core :as re-frame]))

(def interceptors [re-frame/trim-v])

(s/def ::account web3/address?)
(s/def ::accounts (s/coll-of ::account))

(def js->clj-v
  (re-frame/->interceptor
    :id      :js->clj-v
    :before  (fn [context]
               (update-in context [:coeffects :event] js->clj))))

(defn reg-opts-cofx [opts]
  (re-frame/reg-cofx
   :opts
   (fn [coeffects _]
     (assoc coeffects :opts opts))))

(re-frame/reg-event-fx
 ::start
 interceptors
 (fn [{:keys [:db]} [{:keys [:disable-polling? :polling-interval-ms :disable-loading-at-start?]
                      :or {polling-interval-ms 4000}
                      :as opts}]]
   (merge
    {:db (queries/assoc-accounts db [])}
    (when-not disable-loading-at-start?
      {:forward-events {:register ::load-accounts
                        :events #{::web3-events/web3-created}
                        :dispatch-to [::load-accounts opts]}})
    (when (and (not disable-polling?)
               (not disable-loading-at-start?))
      (if (some-> js/window (aget "ethereum") (aget "on"))
        {::effects/watch-accounts {:on-change [::set-accounts]}}
        {:dispatch-interval {:dispatch [::poll-accounts opts]
                             :id ::poll-accounts
                             :ms polling-interval-ms}})))))

(re-frame/reg-event-fx
 ::poll-accounts
 interceptors
 (fn [{:keys [:db]} [opts]]
   (merge
    (when (and (window-focus-queries/focused? db)         ;; Important perf optimisation
               (web3-queries/web3 db))
      {:dispatch [::load-accounts opts]})
    (when (and (web3-queries/web3 db)
               (not (web3-queries/web3-injected? db)))
      {:clear-interval {:id ::poll-accounts}}))))

(re-frame/reg-event-fx
 ::load-accounts
 interceptors
 (fn [{:keys [:db]} [{:keys [:load-injected-accounts-only?]}]]
   (when-let [web3 (web3-queries/web3 db)]
     (if (and load-injected-accounts-only?
              (not (web3-queries/web3-injected? db)))
       {:dispatch [::set-accounts []]}
       {:web3/call {:web3 web3
                    :fns [{:fn web3-eth/accounts
                           :on-success [::set-accounts]
                           :on-error [::accounts-load-failed]}]}}))))

(re-frame/reg-event-fx
 ::accounts-load-failed
 interceptors
 (fn []
   {:dispatch [::set-accounts []]}))

(re-frame/reg-event-fx
 ::set-accounts
 [(re-frame/inject-cofx :opts) interceptors js->clj-v (spec-interceptors/validate-first-arg ::accounts)]
 (fn [{:keys [:db]
       {:keys [:eip55?]} :opts} [accounts]]
   (let [accounts (if eip55? (map eip55/address->checksum accounts) accounts) ;; support for EIP-55, needed ONLY until UI libraries are migrated to web3 1.0 which supports it OOB
         active-account (if (contains? (set accounts) (queries/active-account db))
                          (queries/active-account db)
                          (first accounts))]
     (merge
      (when (not= accounts (queries/accounts db))
        {:db (queries/assoc-accounts db accounts)
         :dispatch [::accounts-changed {:new accounts :old (queries/accounts db)}]})
      {:dispatch-n [[::set-active-account active-account]]}))))

(re-frame/reg-event-fx
 ::accounts-changed
 (constantly nil))

(re-frame/reg-event-fx
 ::set-active-account
 [interceptors (spec-interceptors/validate-first-arg (s/nilable ::account))]
 (fn [{:keys [:db]} [active-account]]
   (when (not= active-account (queries/active-account db))
     {:db (queries/assoc-active-account db active-account)
      :dispatch [::active-account-changed {:new active-account :old (queries/active-account db)}]})))

(re-frame/reg-event-fx
 ::active-account-changed
 (constantly nil))

(re-frame/reg-event-fx
 ::stop
 interceptors
 (fn [{:keys [:db]} [{:keys [:disable-loading-at-start?]}]]
   (merge
    {:db (queries/dissoc-web3-accounts db)
     :clear-interval {:id ::poll-accounts}}
    (when-not disable-loading-at-start?
      {:forward-events {:unregister ::load-accounts}}))))
