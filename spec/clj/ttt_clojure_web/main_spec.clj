(ns ttt-clojure-web.main-spec
  (:require [speclj.core :refer :all]
            [joodo.spec-helpers.controller :refer :all]
            [ttt-clojure-web.main :refer :all]))

(describe "ttt-clojure-web"

  (with-mock-rendering)
  (with-routes app-handler)

  (it "handles root"
    (let [result (do-get "/")]
      (should= 200 (:status result))
      (should= "index" @rendered-template)))
  )

(run-specs)
