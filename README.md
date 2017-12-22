# district-ui-web3-accounts

[![Build Status](https://travis-ci.org/district0x/district-ui-web3-accounts.svg?branch=master)](https://travis-ci.org/district0x/district-ui-web3-accounts)

Clojurescript [mount](https://github.com/tolitius/mount) + [re-frame](https://github.com/Day8/re-frame) module for a district UI, that takes care of handling an user's [web3](https://github.com/ethereum/web3.js/) accounts.

## Installation
Add `[district0x/district-ui-web3-accounts "1.0.0"]` into your project.clj  
Include `[district.ui.web3-accounts]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## district.ui.web3-accounts
This namespace contains web3-accounts [mount](https://github.com/tolitius/mount) module. Once you start mount it'll take care 
of loading web3 accounts.

You can pass following args to initiate this module: 
* `:disable-loading-at-start?` Pass true if you don't want load accounts at start
* `:disable-polling?` Pass true if you want to disable polling for account changes (needed for [MetaMask](https://metamask.io/) account switching)
* `:polling-interval-ms` How often should poll for new accounts. Default: 4000
* `:load-injected-accounts-only?` Pass true if you want to load accounts only when web3 is injected into a browser

```clojure
  (ns my-district.core
    (:require [mount.core :as mount]
              [district.ui.web3-accounts]))

  (-> (mount/with-args
        {:web3 {:url "https://mainnet.infura.io/"}
         :web3-accounts {:polling-interval-ms 5000}})
    (mount/start))
```

## district.ui.web3-accounts.subs
re-frame subscriptions provided by this module:

#### `::accounts`
Returns accounts.

#### `::active-account`
Returns active account. 

```clojure
(ns my-district.home-page
  (:require [district.ui.web3-accounts.subs :as accounts-subs]
            [re-frame.core :refer [subscribe]]))

(defn home-page []
  (let [active-account (subscribe [::accounts-subs/active-account])]
    (fn []
      (if @active-account
        [:div "Your active account is " @active-account]
        [:div "You don't have any active account"]))))
```

## district.ui.web3-accounts.events
re-frame events provided by this module:

#### `::start [opts]`
Event fired at mount start.

#### `::load-accounts [opts]`
Loads web3 accounts

#### `::set-accounts [accounts]`
Sets accounts into db

#### `::poll-accounts [opts]`
Event fired when polling for account changes in an interval. 

#### `::accounts-changed`
Fired when accounts have been changed. Use this event to hook into event flow from your modules.
One example using [re-frame-forward-events-fx](https://github.com/Day8/re-frame-forward-events-fx) may look like this:

```clojure
(ns my-district.events
    (:require [district.ui.web3-accounts.events :as accounts-events]
              [re-frame.core :refer [reg-event-fx]]
              [day8.re-frame.forward-events-fx]))
              
(reg-event-fx
  ::my-event
  (fn []
    {:register :my-forwarder
     :events #{::accounts-events/accounts-changed}
     :dispatch-to [::do-something]}))
```

#### `::set-active-account [active-account]`
Sets active-account into db

#### `::active-account-changed`
Fired when active account has changed. Use this event to hook into event flow from your modules.

#### `::stop`
Cleanup event fired on mount stop.

## district.ui.web3-accounts.queries
DB queries provided by this module:  
*You should use them in your events, instead of trying to get this module's 
data directly with `get-in` into re-frame db.*

#### `accounts [db]`
Returns accounts

```clojure
(ns my-district.events
    (:require [district.ui.web3-accounts.queries :as accounts-queries]
              [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
  ::my-event
  (fn [{:keys [:db]}]
    (if (empty? (accounts-queries/accounts db))
      {:dispatch [::do-something]}
      {:dispatch [::do-other-thing]})))
```

#### `active-account [db]`
Returns active account

#### `assoc-accounts [db accounts]`
Associates accounts and returns new re-frame db.

#### `assoc-active-account [db active-account]`
Associates active account and returns new re-frame db.

#### `dissoc-web3-accounts [db]`
Cleans up this module from re-frame db. 

## Dependency on other district UI modules
* [district-ui-web3](https://github.com/district0x/district-ui-web3)
* [district-ui-window-focus](https://github.com/district0x/district-ui-window-focus)

## Development
```bash
lein deps

# To run tests and rerun on changes
lein doo chrome tests
```