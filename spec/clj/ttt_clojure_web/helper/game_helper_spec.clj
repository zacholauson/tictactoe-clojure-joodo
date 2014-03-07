(ns ttt-clojure-web.helper.game-helper-spec
  (:require [speclj.core :refer :all]
            [ttt-clojure-web.helper.game-helper         :refer                :all]
            [ttt-clojure.interface.player               :refer                :all]
            [ttt-clojure.players.computer               :refer [new-computer] :as computer]
            [ttt-clojure.players.human                  :refer [new-human]    :as human]))

(def human (new-human :x nil))
(def computer (new-computer :o))

(def new-gamestate {:board [:- :- :- :- :- :- :- :- :-],
                    :computer :o,
                    :players [human computer],
                    :options {:difficulty :unbeatable}})

(def mock-request {:session {:gamestate {:board [:x :o :- :- :- :- :- :- :-],
                                         :computer :o,
                                         :options {:difficulty :unbeatable}}},
                   :status 200,
                   :headers {"Location" "/play"},
                   :body ""})

(describe "#parse-int"
  (it "should take a string and return an integer if possible"
    (should= 1 (parse-int "1")))
  (it "should take an integer and return an integer"
    (should= 1 (parse-int 1)))
  (it "should throw an exception if the given string cannot be parsed into an integer"
    (should-throw Exception (parse-int "nope"))))

(describe "#set-gamestate-session"
  (it "should replace the gamestate in the session with the given gamestate"
    (should= {:gamestate {:board [:- :- :- :- :- :- :- :- :-]}}
       (set-gamestate-session mock-request {:board [:- :- :- :- :- :- :- :- :-]}))))

(describe "#build-gamestate"
  (let [params {:board-size 3 :difficulty :unbeatable :who-goes-first :computer}
        built-gamestate (build-gamestate params)]
    (it "should build a gamestate based on the given params"
      (should= [:- :- :- :- :- :- :- :- :-] (:board built-gamestate))
      (should= :x (:computer built-gamestate))
      (should= {:difficulty :unbeatable} (:options built-gamestate)))))

(describe "#session"
  (it "pulls session from a request"
    (should= {:gamestate {:board [:x :o :- :- :- :- :- :- :-], :computer :o, :options {:difficulty :unbeatable}}}
      (session mock-request))))

(describe "#gamestate"
  (it "pulls the gamestate from a request"
    (should= {:board [:x :o :- :- :- :- :- :- :-], :computer :o, :options {:difficulty :unbeatable}}
      (gamestate mock-request))))

(describe "#row-size"
  (it "should return the row size for the given board"
    (should= 3 (row-size [:- :- :- :- :- :- :- :- :-]))))

(describe "#pull-move-from-params"
  (it "should pull the chosen move from the given params and return it in integer form"
    (should= 0 (pull-move-from-params {:move "0"}))
    (should= 3 (pull-move-from-params {:move  3 }))))

(describe "#index-board"
  (it "should return an indexed and formatted board"
    (should= [" 0" " 1" " 2" " 3" " 4" " 5" " 6" " 7" " 8"] (index-board [:- :- :- :- :- :- :- :- :-]))))

(describe "#partitioned-board"
  (it "should partition and index the board"
    (should= [[" 0" " 1" " 2"] [" 3" " 4" " 5"] [" 6" " 7" " 8"]] (partitioned-board {:board [:- :- :- :- :- :- :- :- :-]}))))

(describe "#let-computer-move"
  (it "has the computer make a move"
    (should= {:board [:x :- :- :- :- :- :- :- :-], :computer :o, :players [computer human], :options {:difficulty :unbeatable}}
      (let-computer-move new-gamestate))))

(describe "#build-map-to-send-to-template"
  (let [returned-map (build-map-to-send-to-play-template mock-request)]
    (it "should build the map to send to the play template based on the given request"
      (should= {:board [:x :o :- :- :- :- :- :- :-] :options {:difficulty :unbeatable}, :computer :o} (:gamestate returned-map))
      (should= [2 3 4 5 6 7 8] (:possible-moves returned-map))
      (should= [[" x" " o" " 2"] [" 3" " 4" " 5"] [" 6" " 7" " 8"]] (:indexed-board returned-map))
      (should= false (:game-over? returned-map))
      (should-be-nil (:winner returned-map)))))
