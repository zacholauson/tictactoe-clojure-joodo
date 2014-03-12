(ns ttt-clojure-web.helper.view-helper
   (:require [hiccup.element                     :refer :all]
             [hiccup.form                        :refer :all]
             [hiccup.page                        :refer :all]
             [joodo.env                          :as env]
             [joodo.middleware.asset-fingerprint :refer [add-fingerprint]]
             [joodo.views                        :refer [render-partial *view-context*]]))

(defn space-taken? [string]
  (if (re-find #"x|o" string) true false))

(defn buttonify [string]
  (if (space-taken? string) string
    (if (:game-over? *view-context*) string
      (let [string (clojure.string/trim string)]
        [:div {:class "move-form"}
          (form-to [:post "/move"]
            (text-field    {:type :hidden} :move string)
            (submit-button {:class "move"}       string))]))))
