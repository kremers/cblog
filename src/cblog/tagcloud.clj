(ns cblog.tagcloud
  (:use [cblog.db]))

(defn weight [counted min-count max-count] (/ (- (Math/log counted) (Math/log min-count)) (- (Math/log max-count) (Math/log min-count))))
(defn size [weight min-size max-size] (+ min-size (Math/round (* weight (- max-size min-size)))))
(defn color [weight] (let [b (min (- 255 (Math/round (* weight 255.0))) 150)] (str "rgb(" (+ 1 b) "," (+ 127 b) "," (+ 141 b) ")")))
(defn addweight 
  "Transforms a Structure of :name :count into map with weight and color 
  Input : ({ :name XY :count  2 }  ...) Result: ({ :name XY :count  2 :weight 9 } ...)"
  [input] (let [counters (map #(second (vals %)) input) cmax (apply max counters) cmin (apply min counters)]
            (map #(assoc % :weight (weight (:count %) cmin cmax)) input)))
(defn add-size-and-color [input] (map #(assoc % :color (color (:weight %)) :size (size (:weight %) 9 16)) input))
(defn tagcloud [] (add-size-and-color (addweight (count-tags))))

