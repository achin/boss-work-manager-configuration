(ns boss-work-manager-configuration.ingest.jedoclet
  (:require [boss-work-manager-configuration.ingest :as ingest]
            [clj-xpath.core :as xpath]))

(comment
  (tokenize-camel-symbols (xpath/$x:text+ "//method/@name" xmldoc)))

(comment
  (write-lines "descriptions" (map #(s/replace % #"[}.]" "") (filter #(.matches % "^[^{].*") (mapcat (comp #(concat [":start"] % [":end"]) #(s/split % #"\s+")) (xpath/$x:text+ "//comment/description" xmldoc))))))

(comment
  (def class-name-tokens (line-seq (clojure.java.io/reader "class-names"))))
