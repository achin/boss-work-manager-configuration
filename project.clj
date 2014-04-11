(defproject boss-work-manager-configuration "0.1.0-SNAPSHOT"
  :description "A Markov model and chain generator for creating random Java symbols"
  :url "https://github.com/achin/boss-work-manager-configuration"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.2.2"]
                 [ring/ring-json "0.3.0"]
                 [compojure "1.1.6"]
                 [com.github.kyleburton/clj-xpath "1.4.3"]]
  :uberjar-name "boss-work-manager-configuration.jar")
