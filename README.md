# district-ui-web3-accounts

[![CircleCI](https://circleci.com/gh/district0x/district-ui-web3-accounts.svg?style=svg)](https://circleci.com/gh/district0x/district-ui-web3-accounts)

Clojurescript [re-mount](https://github.com/district0x/d0x-INFRA/blob/master/re-mount.md) module, that takes care of handling an user's [web3](https://github.com/ethereum/web3.js/) accounts.

## Installation
Add
[![Clojars Project](https://img.shields.io/clojars/v/io.github.district0x/district-ui-web3-accounts.svg?include_prereleases)](https://clojars.org/io.github.district0x/district-ui-web3-accounts)
into your project.clj
Include `[district.ui.web3-accounts]` in your CLJS file, where you use `mount/start`

## API Overview

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

- [district.ui.web3-accounts](#districtuiweb3-accounts)
- [district.ui.web3-accounts.subs](#districtuiweb3-accountssubs)
  - [::accounts](#accounts-sub)
  - [::has-accounts?](#has-accounts?-sub)
  - [::active-account](#active-account-sub)
  - [::has-active-account?](#has-active-account?-sub)
- [district.ui.web3-accounts.events](#districtuiweb3-accountsevents)
  - [::load-accounts](#load-accounts)
  - [::set-accounts](#set-accounts)
  - [::poll-accounts](#poll-accounts)
  - [::load-accounts](#load-accounts)
  - [::accounts-changed](#accounts-changed)
  - [::set-active-account](#set-active-account)
  - [::active-account-changed](#active-account-changed)
- [district.ui.web3-accounts.queries](#districtuiweb3-accountsqueries)
  - [accounts](#accounts)
  - [has-accounts?](#has-accounts?)
  - [active-account](#active-account)
  - [has-active-account?](#has-active-account?)
  - [assoc-accounts](#assoc-accounts)
  - [assoc-active-account](#assoc-active-account)

## district.ui.web3-accounts
This namespace contains web3-accounts [mount](https://github.com/tolitius/mount) module. Once you start mount it'll take care
of loading web3 accounts.

Note, since version `1.0.6` polling for address changes is only fallback option, since MetaMask now provides callback
registration for [accounts changed event](https://metamask.github.io/metamask-docs/API_Reference/Ethereum_Provider#ethereum.on(eventname%2C-callback)),
which is automatically used if available.

You can pass following args to initiate this module:
* `:disable-loading-at-start?` Pass true if you don't want load accounts at start
* `:disable-polling?` Pass true if you want to disable polling for account changes (needed for [MetaMask](https://metamask.io/) account switching)
* `:polling-interval-ms` How often should poll for new accounts. Default: 4000
* `:load-injected-accounts-only?` Pass true if you want to load accounts only when web3 is injected into a browser
* `:eip55?` Pass true if you want to turn on support for the [EIP-55](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-55.md).

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

#### <a name="accounts-sub">`::accounts`
Returns accounts.

#### <a name="has-accounts?-sub">`::has-accounts?`
Returns true if user has accounts.

#### <a name="active-account-sub">`::active-account`
Returns active account.

#### <a name="has-active-account?-sub">`::has-active-account?`
Returns true if user has active account.

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

#### <a name="load-accounts">`::load-accounts [opts]`
Loads web3 accounts

#### <a name="set-accounts">`::set-accounts [accounts]`
Sets accounts into db

#### <a name="poll-accounts">`::poll-accounts [opts]`
Event fired when polling for account changes in an interval. Note, polling is now used only as fallback
option, since MetaMask now provides callback registration for [accounts changed event](https://metamask.github.io/metamask-docs/API_Reference/Ethereum_Provider#ethereum.on(eventname%2C-callback)).

#### <a name="accounts-changed">`::accounts-changed`
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

#### <a name="set-active-account">`::set-active-account [active-account]`
Sets active-account into db

#### <a name="active-account-changed">`::active-account-changed`
Fired when active account has changed. Use this event to hook into event flow from your modules.

## district.ui.web3-accounts.queries
DB queries provided by this module:
*You should use them in your events, instead of trying to get this module's
data directly with `get-in` into re-frame db.*

#### <a name="accounts">`accounts [db]`
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

#### <a name="has-accounts?">`has-accounts? [db]`
Returns true if user has accounts.

#### <a name="active-account">`active-account [db]`
Returns active account

#### <a name="has-active-account?">`has-active-account? [db]`
Returns true if user has active account.

#### <a name="assoc-accounts">`assoc-accounts [db accounts]`
Associates accounts and returns new re-frame db.

#### <a name="assoc-active-account">`assoc-active-account [db active-account]`
Associates active account and returns new re-frame db.

## Dependency on other district UI modules
* [district-ui-web3](https://github.com/district0x/district-ui-web3)
* [district-ui-window-focus](https://github.com/district0x/district-ui-window-focus)

## Development

1. Run test suite:
- Browser
  - `npx shadow-cljs watch test-browser`
  - open https://d0x-vm:6502
  - tests refresh automatically on code change
- CI (Headless Chrome, Karma)
  - `npx shadow-cljs compile test-ci`
  - ``CHROME_BIN=`which chromium-browser` npx karma start karma.conf.js --single-run``

2. Build
- on merging pull request to master on GitHub, CI builds & publishes new version automatically
- update version in `build.clj`
- to build: `clj -T:build jar`
- to release: `clj -T:build deploy` (needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` env vars to be set)