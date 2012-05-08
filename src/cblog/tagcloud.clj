(ns cblog.tagcloud)

(defn weight [counted min-count max-count] (/ (- (Math/log counted) (Math/log min-count)) (- (Math/log max-count) (Math/log min-count))))
(defn size [weight min-size max-size] (+ min-size (Math/round (* weight (- max-size min-size)))))
(defn color [weight] (let [b (min (- 255 (Math/round ((* weight 1.0)  255))) 200)] (str "rgb(" b "," b "," b ")")))

(defn tagcloud 
  "Transforms a Structure of :name :count into map with weight and color 
  Input : ({ :name XY :count  2 }  ...)
  Result: ({ :name XY :weight 9 } ...)"
  [input] (let [counters (map #(second (vals %)) input) cmax (apply max counters) cmin (apply min counters)]
             (map #(assoc % :weight (weight (:count %) cmin cmax)) input)))

