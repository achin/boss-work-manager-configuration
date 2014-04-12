(ns boss-work-manager-configuration.app
  (:require [boss-work-manager-configuration.markov :as markov]
            [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as json]
            [ring.util.response :as response]))

(defn load-model
  "Loads a token file and returns a Markov model using mk-model."
  [path]
  (with-open [r (io/reader path)]
    (let [tokens (line-seq r)
          model (markov/mk-model tokens)
          real-chains (apply hash-set (markov/chains tokens))]
      {:model model
       :real-chains real-chains})))

(defroutes main-routes
  (GET "/" []
    (response/resource-response "index.html" {:root "public"}))
  (GET "/quiz" {:as r}
    (response/response (markov/quiz (::model r)
                                    (::real-chains r))))
  (route/resources "/"))

(defn wrap-data
  "Adds arbitrary data to a Ring request map."
  [handler data-map]
  (fn [req]
    (handler (merge req data-map))))

(defn web-app
  "Creates a new web app using the given token file to populate a Markov model."
  [path]
  (let [{:keys [model real-chains]} (load-model path)]
    (-> main-routes
        json/wrap-json-response
        (wrap-data {::model model ::real-chains real-chains}))))

(defn configure
  "Configures a Jetty instance by setting graceful shutdown."
  [jetty]
  (.setGracefulShutdown jetty (* 60 1000))
  (.setStopAtShutdown jetty true))

(defn run-web-app
  "Runs a webapp on the given port and joins with the current thread, if specified."
  [port join?]
  (jetty/run-jetty (web-app "resources/class-names")
                   {:port port
                    :join? join?
                    :configurator configure}))

(defn -main [port]
  (run-web-app (Integer. port) true))
