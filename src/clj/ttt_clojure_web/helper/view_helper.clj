(ns ttt-clojure-web.helper.view-helper
   "Put helper functions for views in this namespace."
   (:require [hiccup.element :refer :all]
             [hiccup.form :refer :all]
             [hiccup.page :refer :all]
             [joodo.env :as env]
             [joodo.middleware.asset-fingerprint :refer [add-fingerprint]]
             [joodo.views :refer [render-partial *view-context*]]))

(defn space-taken? [string]
  (if (re-find #"x|o" string) true false))

(defn buttonify [string]
  (if (space-taken? string) string
    [:div 
     (label string string)
     (radio-button :move true string)]))
