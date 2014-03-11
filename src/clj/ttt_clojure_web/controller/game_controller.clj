(ns ttt-clojure-web.controller.game-controller
  (:require [compojure.core                     :refer :all]
            [joodo.views                        :refer (render-template)]
            [joodo.middleware.request           :refer [*request*]]
            [ring.util.response                 :refer [redirect]]
            [ttt-clojure.gamestate              :refer [move game-over?]]
            [ttt-clojure-web.helper.game-helper :as    helper])
  (:use [hiccup.form]
        [hiccup.element]))

(defn start-game [request params]
  (assoc (redirect "/play") :session (helper/set-gamestate-session request (helper/build-gamestate params))))

(defn make-play [params request]
  (let [new-move  (helper/pull-move-from-params params)
        gamestate (move (helper/gamestate request) new-move)]
       (assoc
         (redirect "/play")
         :session (helper/set-gamestate-session request gamestate))))

(defn computer-turn? [request]
  (= (str (type (first (:players (helper/gamestate request))))) "class ttt_clojure.players.computer.Computer"))

(defn play-action [request]
  (if-not (helper/gamestate request) (redirect "/")
    (if (game-over? (helper/gamestate request)) (render-template "index" (helper/build-map-to-send-to-play-template request) :ns `ttt-clojure-web.helper.view-helper)
      (if (computer-turn? request)
        (assoc
          (redirect "/play")
          :session (helper/set-gamestate-session request (helper/let-computer-move (helper/gamestate request))))
        (render-template "index" (helper/build-map-to-send-to-play-template request) :ns `ttt-clojure-web.helper.view-helper)))))

(defroutes game-controller
  (POST "/new-game" {params :params} (start-game *request* params))
  (POST "/move"     {params :params} (make-play params *request*))
  (GET  "/play"     []               (play-action *request*)))
