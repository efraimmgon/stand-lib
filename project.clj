(defproject stand-lib "0.1.0-SNAPSHOT"
  :description "Helpers for Web Dev with re-frame"
  :url "http://github.com/efraimmgon/stand-lib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :clojurescript? true
  :plugins [[lein-cljsbuild "1.1.1"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]
                 [prismatic/dommy "1.1.0"]
                 [re-frame "0.10.2"]
                 [reagent "0.7.0"]])
