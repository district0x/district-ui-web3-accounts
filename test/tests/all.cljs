(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [day8.re-frame.test :refer [run-test-async wait-for run-test-sync]]
    [district.ui.web3-accounts.events :as events]
    [district.ui.web3-accounts.subs :as subs]
    [district.ui.web3-accounts]
    [mount.core :as mount]
    [re-frame.core :refer [reg-event-fx dispatch-sync subscribe reg-cofx reg-fx dispatch]]))

(defn set-response [accounts]
  (reg-fx
    :web3/call
    (fn [{:keys [:fns]}]
      (dispatch (vec (concat (:on-success (first fns))
                             [accounts]))))))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})


(deftest tests
  (run-test-async
    (let [accounts (subscribe [::subs/accounts])
          active-account (subscribe [::subs/active-account])
          mock-accounts ["0x4a5c034cc587a219e5099eac1e7b92f468b77129"
                         "0x701de50ef02bd981feccc1cc07b8938a1a8d64c2"]
          mock-accounts2 ["0x93023b437dc89769e0088ceafb9f7cdbf149814c"]]

      (set-response mock-accounts)

      (-> (mount/with-args
            {:web3 {:url "https://mainnet.infura.io/"}
             :web3-accounts {}})
        (mount/start))

      (wait-for [::events/active-account-changed ::events/accounts-load-failed]
        (is (= @accounts mock-accounts))
        (is (= @active-account (first mock-accounts)))

        (dispatch [::events/set-active-account (second mock-accounts)])

        (wait-for [::events/active-account-changed]
          (is (= @accounts mock-accounts))
          (is (= @active-account (second mock-accounts)))

          (set-response mock-accounts2)

          (dispatch [::events/poll-accounts])

          (wait-for [::events/active-account-changed]
            (is (= @accounts mock-accounts2))
            (is (= @active-account (first mock-accounts2)))))))))

(deftest invalid-params-tests
  (run-test-sync
    (-> (mount/with-args
          {:web3 {:url "https://mainnet.infura.io/"}
           :web3-accounts {}})
      (mount/start))

    (is (thrown? :default (dispatch-sync [::events/set-accounts [nil]])))
    (is (thrown? :default (dispatch-sync [::events/set-active-account 1])))))

(deftest disable-loading-at-start-tests
  (run-test-sync

    (set-response ["0x93023b437dc89769e0088ceafb9f7cdbf149814c"])

    (-> (mount/with-args
          {:web3 {:url "https://mainnet.infura.io/"}
           :web3-accounts {:disable-loading-at-start? true}})
      (mount/start))

    (is (empty? @(subscribe [::subs/accounts])))
    (is (nil? @(subscribe [::subs/active-account])))))