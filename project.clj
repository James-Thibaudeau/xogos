(defproject xogos "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [secretary "1.2.3"]
                 [cljsjs/howler "2.0.5-0"]
                 [bulma-cljs "0.1.4"]]

  :repl-options {:init-ns dev.user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-less "1.7.5"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :less {:source-paths ["less"]
         :target-path "resources/public/css"}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [com.cemerick/piggieback "0.2.1"]
                   [figwheel-sidecar "0.5.16"]]

    :plugins [[lein-figwheel "0.5.18"]
              [lein-doo "0.1.8"]]}
   :prod {}}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs"]
     :figwheel {:on-jsload "xogos.core/mount-root"}
     :compiler {:main xogos.core
                :output-to "resources/public/js/compiled/app.js"
                :output-dir "resources/public/js/compiled/out"
                :asset-path "js/compiled/out"
                :source-map-timestamp true
                :preloads [devtools.preload]
                :external-config {:devtools/config {:features-to-install :all}}}}

    {:id "min"
     :source-paths ["src/cljs"]
     :compiler {:main xogos.core
                :output-to "resources/public/js/compiled/app.js"
                :optimizations :advanced
                :closure-defines {goog.DEBUG false}
                :pretty-print false}}

    {:id "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler {:main xogos.runner
                :output-to "resources/public/js/compiled/test.js"
                :output-dir "resources/public/js/compiled/test/out"
                :optimizations :none}}]})
