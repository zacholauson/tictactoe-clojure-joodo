(ns ttt-clojure-web.controller.game-controller
  (:require [compojure.core                     :refer :all]
            [joodo.views                        :refer (render-template)]
            [joodo.middleware.request           :refer [*request*]]
            [ring.util.response                 :refer [redirect]]
            [ttt-clojure.gamestate              :refer [move]]
            [ttt-clojure-web.helper.game-helper :as helper])
  (:use [hiccup.form]
        [hiccup.element]))

(defn start-game [request params]
  (let [gamestate (helper/build-gamestate params)
        gamestate (helper/computer-move-if-computer-turn gamestate)]
       (assoc 
         (redirect "/play") 
         :session (helper/set-gamestate-session request gamestate))))

(defn make-human-move [params request]
  (let [new-move  (helper/pull-move-from-params params)
        gamestate (move (helper/gamestate request) new-move)
        gamestate (helper/let-computer-move gamestate)]
    (assoc
      (redirect "/play")
      :session (helper/set-gamestate-session request gamestate))))

(defroutes game-controller
  (POST "/new-game" {params :params} (start-game *request* params))
  (POST "/move"     {params :params} (make-human-move params *request*))
  (GET  "/play"     []               (if-not (helper/gamestate *request*)
                                       (redirect "/") 
                                       (render-template "index" (helper/build-map-to-send-to-play-template *request*) :ns `ttt-clojure-web.helper.view-helper))))
