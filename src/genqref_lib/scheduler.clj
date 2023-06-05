(ns genqref-lib.scheduler
  (:require [genqref-lib.time :as t]
            [genqref-lib.util :as util]
            [taoensso.timbre :as timbre :refer [debug trace]])
  (:gen-class))

(defonce ^:private schedule (atom {}))

(defn- due-action? [[ts f]]
  (pos? (compare (t/now) ts)))

(defn start! []
  (debug "Starting scheduler at UTC:" (t/now))
  (util/safe-future
   (while true
     (util/sleep 1)
     (let [due (filter due-action? @schedule)]
       (doseq [[time f] due]
         (do
           (trace "Perform function call scheduled for" time)
           (util/safe-future (f))
           (swap! schedule dissoc time)))))))

;; TODO: this works but should probably be a macro instead so we can
;; retain a human readable and more important seriazable version of
;; `f`
(defn schedule! [time f]
  (trace "Scheduling function call for" time)
  (swap! schedule assoc time f))

(defn cancel-everything! []
  (reset! schedule {}))
