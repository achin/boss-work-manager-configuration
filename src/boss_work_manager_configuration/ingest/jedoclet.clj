(ns boss-work-manager-configuration.ingest.jedoclet
  (:require [boss-work-manager-configuration.ingest :as ingest]
            [clojure.java.io :as io]
            [clj-xpath.core :as xpath]))

(defn constructors
  "Returns a sequence of constructor names from a jedoclet-generated xml document."
  [xmldoc]
  (xpath/$x:text+ "//method/@name" xmldoc))

(defn write-constructor-tokens
  "Reads the jedoclet xml doc specified by xml-path, parses token constructors,
  and writes them to output-path."
  [xml-path output-path]
  (with-open [f (io/reader xml-path)]
    (->> (slurp f)
         xpath/xml->doc
         constructors
         (ingest/write-tokens output-path ingest/camel-seq))))
