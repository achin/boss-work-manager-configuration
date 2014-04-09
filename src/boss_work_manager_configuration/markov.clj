(def camel-seq
  (partial re-seq #"(?:^[a-z]|[A-Z])[^A-Z]+|[A-Z]+$|[A-Z]{2,}(?=[A-Z])"))

(defn write-lines [path lines]
  (with-open [f (clojure.java.io/writer path)]
    (doseq [l lines]
      (.write f l)
      (.newLine f))))

(defn tokenize-camel-symbols [coll]
  (mapcat (comp #(concat [":start"] % [":end"])
                camel-seq)
          coll))

(tokenize-camel-symbols (xpath/$x:text+ "//method/@name" xmldoc))

(write-lines "descriptions" (map #(s/replace % #"[}.]" "") (filter #(.matches % "^[^{].*") (mapcat (comp #(concat [":start"] % [":end"]) #(s/split % #"\s+")) (xpath/$x:text+ "//comment/description" xmldoc)))))

(def class-name-tokens (line-seq (clojure.java.io/reader "class-names")))

(defn invalid-token-pair? [[a b]] (or (= ":start" b) (= ":end" a)))

(defn mk-markov [tokens]
  (->> (partition 2 1 tokens)
       (remove invalid-token-pair?)
       (reduce (fn [m [a b]]
                 (update-in m [a b] (fnil inc 0)))
               {})
       (map (fn [[a m]]
              (let [total (apply + (vals m))]
                [a (into {}
                         (for [[w k] m]
                           [w (/ k total)]))])))
       (into {})))

(defn cumulative-transitions [trans]
  (loop [trans (seq model)
         ctrans (sorted-map)
         total 0]
    (let [[[state p] & rtrans] trans
          q (+ p total)
          ctrans (assoc ctrans q state)]
      (if rtrans
        (recur rtrans ctrans q)
        ctrans))))

(defn random-transition [ctrans]
  (let [r (rand)]
    (val (first (filter #(> (key %) r) ctrans)))))

(defn next-state [model current-state]
  (-> (get model current-state)
      cumulative-transitions
      random-transition))

(defn markov-chain [model sep]
  (->> (iterate (partial next-state model) ":start")
       (take-while (partial not= ":end"))
       (drop 1)
       (s/join sep)))

(defn chains [tokens]
  (->> tokens
       (remove (partial = ":start"))
       (partition-by (partial = ":end"))
       (map (partial apply str))
       (remove (partial = ":end"))
       (apply hash-set)))

(defn amuse-me [path sep n]
  (with-open [r (clojure.java.io/reader path)]
    (let [tokens (line-seq r)
          existing-token? (chains tokens)
          model (mk-markov tokens)]
      (->> (repeatedly #(markov-chain model sep))
           (filter (complement existing-token?))
           (take n)))))

(defn real-or-not [path sep n]
  (with-open [r (clojure.java.io/reader path)]
    (let [tokens (line-seq r)
          existing-token? (chains tokens)
          model (mk-markov tokens)
          fakes (->> (repeatedly #(markov-chain model sep))
                     (filter (complement existing-token?)))
          fakes2 (->> (repeatedly #(markov-chain model sep))
                      (filter (complement existing-token?)))]
      (->> (map (juxt (comp shuffle vector)
                      (fn [& args] (first args)))
               (shuffle existing-token?)
                fakes
                fakes2)
           (take n)))))
