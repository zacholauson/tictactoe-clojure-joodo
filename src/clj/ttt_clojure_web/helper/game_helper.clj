(ns ttt-clojure-web.helper.game-helper
  (:require [ttt-clojure.interface.player  :refer [piece]]
            [ttt-clojure.players.computer  :refer [new-computer] :as computer]
            [ttt-clojure.players.human     :refer [new-human]    :as human]
            [ttt-clojure.gamestate         :refer [move create-board possible-moves game-over? winner]]
            [ttt-clojure.ai                :refer [find-move]]
            [clojure.math.numeric-tower    :refer [sqrt]         :as math]))

(defn parse-int [string]
  (cond
    (string? string) (Integer. (re-find  #"\d+" string ))
    (integer? string) string
    :else (throw (Exception. "cannot convert given argument to integer"))))

(defn set-gamestate-session [request gamestate]
  (assoc (:session request) :gamestate gamestate))

(defn build-gamestate [params]
  (let [row-size       (parse-int (:board-size params))
        difficulty     (keyword   (:difficulty params))
        who-goes-first (keyword   (:who-goes-first params))
        computer       (computer/new-computer (if (= who-goes-first :computer) :x :o))
        human          (human/new-human (if (= who-goes-first :computer) :o :x) nil)
        player-col     (if (= who-goes-first :computer) [computer human] [human computer])
        computer-mark  (if (= who-goes-first :computer) :x :o)]
    {:board (create-board row-size)
     :players player-col
     :computer computer-mark
     :options {:difficulty difficulty}}))

(defn session [request]
  (:session request))

(defn gamestate [request]
  (:gamestate (session request)))

(defn row-size [board]
  (math/sqrt (count board)))

(defn pull-move-from-params [params]
  (parse-int (:move params)))

(defn index-board [board]
  (map-indexed #(if (= :- %2) (format "%2s" %1) (format "%2s" (name %2))) board))

(defn partitioned-board [gamestate]
  (partition (row-size (:board gamestate)) (index-board (:board gamestate))))

(defn let-computer-move [gamestate]
  (move gamestate (find-move gamestate)))

(defn computer-move-if-computer-turn [gamestate]
  (if (= :x (:computer gamestate))
      (let-computer-move gamestate)
      gamestate))

(defn build-map-to-send-to-play-template [request]
  (let [gamestate    (gamestate request)]
    {:gamestate      gamestate
     :possible-moves (possible-moves    gamestate)
     :indexed-board  (partitioned-board gamestate)
     :game-over?     (game-over?        gamestate)
     :winner         (winner            gamestate)}))
