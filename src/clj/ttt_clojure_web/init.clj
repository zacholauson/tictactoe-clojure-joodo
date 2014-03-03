(ns ttt-clojure-web.init
  (:require [joodo.env :as env]))

(defn init []
  (env/load-configurations))
