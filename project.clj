(defproject district0x/district-ui-web3-accounts "1.0.7-SNAPSHOT"
  :description "district UI module for handling web3 accounts"
  :url "https://github.com/district0x/district-ui-web3-accounts"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cljs-web3 "0.19.0-0-10"]
                 [day8.re-frame/forward-events-fx "0.0.6"]
                 [district0x/district-ui-web3 "1.3.2"]
                 [district0x/district-ui-window-focus "1.0.0"]
                 [district0x/eip55 "0.0.1"]
                 [district0x/re-frame-interval-fx "1.0.2"]
                 [district0x/re-frame-spec-interceptors "1.0.1"]
                 [district0x.re-frame/web3-fx "1.0.5"]
                 [mount "0.1.16"]
                 [org.clojure/clojurescript "1.10.597"]
                 [re-frame "0.11.0"]]

  :doo {:paths {:karma "./node_modules/karma/bin/karma"}}

  :npm {:devDependencies [[karma "1.7.1"]
                          [karma-chrome-launcher "2.2.0"]
                          [karma-cli "1.0.1"]
                          [karma-cljs-test "0.1.0"]]}

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [day8.re-frame/test "0.1.5"]]
                   :source-paths ["src" "test"]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-doo "0.1.8"]
                             [lein-npm "0.6.2"]]}}

  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["deploy"]]

  :cljsbuild {:builds [{:id "tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "tests-output/tests.js"
                                   :output-dir "tests-output"
                                   :main "tests.runner"
                                   :optimizations :none}}]})
