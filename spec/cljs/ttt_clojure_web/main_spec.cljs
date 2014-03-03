(ns ttt-clojure-web.main-spec
  (:require-macros [hiccups.core :as h]
                   [specljs.core :refer [around before context describe it should-contain should-not= should= with]])
  (:require [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [specljs.core]
            [ttt-clojure-web.main :as main]))

(describe "Main"

  (it "foo"
    (should= :foo (main/foo)))

  )
