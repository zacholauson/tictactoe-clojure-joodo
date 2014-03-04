(ns ttt-clojure-web.controller.game-controller
  (:require [compojure.core                 :refer :all]
            [joodo.views                    :refer (render-template)]
            [joodo.middleware.request       :refer [*request*]]
            [ring.util.response             :refer [redirect]]
            [hiccup.core                    :refer :all]
            [clojure.math.numeric-tower     :as math]
            [ttt-clojure.interface.player   :refer :all]
            [ttt-clojure.players.computer   :refer [new-computer] :as computer]
            [ttt-clojure.players.human      :refer [new-human]    :as human]
            [ttt-clojure.interface.display  :refer :all]
            [ttt-clojure.displays.web       :refer [new-display-web]]
            [ttt-clojure.interface.prompter :refer :all]
            [ttt-clojure.prompters.web      :refer [new-web-prompter]]
            [ttt-clojure.gamestate          :refer [create-board]]
            [ttt-clojure.core               :refer :all]
            [ttt-clojure.ai                 :refer :all]
            [ttt-clojure.interface.player   :refer :all]
            [ttt-clojure.gamestate          :refer :all])
  (:use [hiccup.form]
        [hiccup.element]))

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
    {:board (create-board row-size) :players player-col :computer computer-mark :options {:difficulty difficulty}}))

(defn session [request]
  (:session request))

(defn gamestate [request]
  (:gamestate (session request)))

(defn row-size [board]
  (math/sqrt (count board)))

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

(defn start-game [request params]
  (let [gamestate (build-gamestate params)
        gamestate (computer-move-if-computer-turn gamestate)]
       (assoc (redirect "/play") :session (set-gamestate-session request gamestate))))

(defn build-map-to-send-to-play-template [request]
  {:gamestate      (gamestate request)
   :possible-moves (possible-moves (gamestate request))
   :indexed-board  (partitioned-board (gamestate request))
   :game-over?     (game-over? (gamestate request))
   :winner         (winner (gamestate request))})

(defn make-human-move [params request]
  (let [new-move (parse-int (:move params))
        gamestate (move (gamestate request) new-move)
        gamestate (let-computer-move gamestate)]
    (assoc (redirect "/play") :session (set-gamestate-session request gamestate))))

(defroutes game-controller
  (POST "/new-game" {params :params} (start-game *request* params))
  (GET  "/play"     []               (if (not (gamestate *request*)) (redirect "/") (render-template "play" (build-map-to-send-to-play-template *request*))))
  (POST "/move"     {params :params} (make-human-move params *request*)))
