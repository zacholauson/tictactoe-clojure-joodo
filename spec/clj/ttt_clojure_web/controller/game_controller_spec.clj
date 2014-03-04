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

(def new-gamestate {:board [:- :- :- :- :- :- :- :- :-], :computer :o, :players [human computer]  :options {:difficulty :unbeatable}})

(def mock-request {:session {:gamestate {:board [:x :o :- :- :- :- :- :- :-], :computer :o, :options {:difficulty :unbeatable}}}, :status 302, :headers {"Location" "/play"}, :body ""})

(describe "game-controller"
  (with-mock-rendering)
  (with-routes app-handler)

  (describe "#session"
    (it "pulls session from a request"
      (should= {:gamestate {:board [:x :o :- :- :- :- :- :- :-], :computer :o, :options {:difficulty :unbeatable}}}
               (session mock-request))))

  (describe "#gamestate"
    (it "pulls the gamestate from a request"
      (should= {:board [:x :o :- :- :- :- :- :- :-], :computer :o, :options {:difficulty :unbeatable}}
               (gamestate mock-request))))

  (describe "#let-computer-move"
    (it "has the computer make a move"
      (should= {:board [:x :- :- :- :- :- :- :- :-], :computer :o, :players [computer human], :options {:difficulty :unbeatable}} (let-computer-move new-gamestate))))

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
        (should= "play" @rendered-template)))

    (it "handles /move and moves the gamestate to the humans chosen move"
      (let [result (request :post "/move" :session {:gamestate new-gamestate} :params {:move "0"})]
        (should= [:x :o :- :- :- :- :- :- :-] (-> result :session :gamestate :board))))))

