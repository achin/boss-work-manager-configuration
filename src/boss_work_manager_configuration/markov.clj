(ns boss-work-manager-configuration.markov)

(defn invalid-token-pair?
  "Returns true if the given token pair ends with a start token or starts with
  an end token."
  [[a b]] (or (= ":start" b) (= ":end" a)))

(defn mk-markov
  "Makes a Markov model from a sequence of tokens."
  [tokens]
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

(defn cumulative-transitions
  "Transforms a transition map from a map of token to probability to a map
  of token to cumulative probability. e.g.

  (cumulative-transitions {\"a\" 1/4
                           \"b\" 1/2
                           \"c\" 1/4})
  => {\"a\" 1/4
      \"b\" 3/4
      \"c\" 4/4}"
  [trans]
  (loop [trans (seq model)
         ctrans (sorted-map)
         total 0]
    (let [[[state p] & rtrans] trans
          q (+ p total)
          ctrans (assoc ctrans q state)]
      (if rtrans
        (recur rtrans ctrans q)
        ctrans))))

(defn random-token
  "Selects a random token to given a cumulative transition map.  If you have
  a normal transition map as created by mk-markov, you can create a cumulative
  transition map from it by using cumulative-transitions."
  [ctrans]
  (let [r (rand)]
    (val (first (filter #(> (key %) r) ctrans)))))

(defn next-state
  "Selects a random state to transition to given a model (created through
  mk-markov) and a current state."
  [model current-state]
  (-> (get model current-state)
      cumulative-transitions
      random-token))

(defn markov-chain
  "Creates a random chain of tokens given a model (created through mk-markov) and
  joins the tokens with the given separator."
  [model sep]
  (->> (iterate (partial next-state model) ":start")
       (take-while (partial not= ":end"))
       (drop 1)
       (s/join sep)))

(defn chains
  "Returns a hash set of the original token chains represented by a stream of
  tokens as ingested by boss-work-manager-configuration.ingest."
  [tokens]
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
