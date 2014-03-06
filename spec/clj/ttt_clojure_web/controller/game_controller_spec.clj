(ns ttt-clojure-web.controller.game-controller-spec
  (:require [speclj.core                                :refer :all]
            [joodo.spec-helpers.controller              :refer :all]
            [ttt-clojure-web.main                       :refer :all]
            [ttt-clojure.interface.player               :refer :all]
            [ttt-clojure.players.computer               :refer [new-computer] :as computer]
            [ttt-clojure.players.human                  :refer [new-human]    :as human]
            [ttt-clojure-web.controller.game-controller :refer :all]))

(def human (new-human :x nil))
(def computer (new-computer :o))

(def new-gamestate {:board [:- :- :- :- :- :- :- :- :-],
                    :computer :o,
                    :players [human computer],
                    :options {:difficulty :unbeatable}})

(describe "game-controller"
  (with-mock-rendering)
  (with-routes app-handler)

  (describe "routes"
    (it "handles /"
      (let [result (do-get "/")]
        (should= 200 (:status result))
        (should= "index" @rendered-template)))

    (it "handles /play when gamestate does not exist in session"
      (let [result (do-get "/play")]
        (should= 302 (:status result))
        (should= "/" ((:headers result) "Location"))))

    (it "handles /play when gamestate does exist in session"
      (let [request-result (request :get "/play" :session {:gamestate new-gamestate})]
        (should= 200 (:status request-result))
        (should= "game/play" @rendered-template)))

    (it "handles /move and moves the gamestate to the humans chosen move"
      (let [result (request :post "/move" :session {:gamestate new-gamestate} :params {:move "0"})]
        (should= [:x :o :- :- :- :- :- :- :-] (-> result :session :gamestate :board))))))

