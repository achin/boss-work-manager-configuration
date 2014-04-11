(ns boss-work-manager-configuration.app
  (:require [boss-work-manager-configuration.markov :as markov]
            [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as json]
            [ring.util.response :as response]))

(defn load-model
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
  [handler data-map]
  (fn [req]
    (handler (merge req data-map))))

(defn web-app
  [path]
  (let [{:keys [model real-chains]} (load-model path)]
    (-> main-routes
        json/wrap-json-response
        (wrap-data {::model model ::real-chains real-chains}))))

(defn run-web-app
  []
  (jetty/run-jetty (web-app "resources/class-names")
                   {:port 8080
                    :join? false}))
