(ns angularexample.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:use-macros [purnam.core :only [obj arr ! def.n]]
               [gyr.core :only [def.module def.config def.factory
                                def.controller def.service]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [re-frame.db    :refer [app-db]]))

;; trigger a dispatch every second
(defonce time-updater (js/setInterval
                        #(dispatch [:timer (js/Date.)]) 1000))

(def initial-state
  {:timer (js/Date.)
   :time-color "#f34"})


;; -- Event Handlers ----------------------------------------------------------


(register-handler                 ;; setup initial state
  :initialize                     ;; usage:  (submit [:initialize])
  (fn 
    [db _]
    (merge db initial-state)))    ;; what it returns becomes the new state


(register-handler
  :time-color                     ;; usage:  (submit [:time-color 34562])
  (path [:time-color])            ;; this is middleware
  (fn
    [time-color [_ value]]        ;; path middleware adjusts the first parameter
    value))


(register-handler
  :timer
  (fn
    ;; the first item in the second argument is :timer the second is the 
    ;; new value
    [db [_ value]]
    (assoc db :timer value)))    ;; return the new version of db


;; -- Subscription Handlers ---------------------------------------------------


(register-sub
  :timer
  (fn 
    [db _]                       ;; db is the app-db atom
    (reaction (:timer @db))))    ;; wrap the compitation in a reaction


(register-sub
  :time-color
  (fn 
    [db _]
    (reaction (:time-color @db))))


;; -- View Components ---------------------------------------------------------

(defn greeting
  [message]
  [:h1 message])


(defn clock
  []
  (let [time-color (subscribe [:time-color])
        timer (subscribe [:timer])]

    (fn clock-render
        []
        (let [time-str (-> @timer
                           .toTimeString
                           (clojure.string/split " ")
                           first)
              style {:style {:color @time-color}}]
             [:div.example-clock style time-str]))))


(defn color-input
  []
  (let [time-color (subscribe [:time-color])]

    (fn color-input-render
        []
        [:div.color-input
         "Time color: "
         [:input {:type "text"
                  :value @time-color
                  :on-change #(dispatch
                               [:time-color (-> % .-target .-value)])}]])))

(defn simple-example
  []
  [:div
   [greeting "Reagent: Normal."]
   [clock]
   [color-input]])


;; -- Angular Module + Test Controllers

(def.module angularTest [])

(def.controller angularTest.AngularWatchCtrl [$scope $timeout]

                ;; $scope vars
                (! $scope.timeStr "")
                (! $scope.timeColor "#f34")

                ;; $scope functions
                (! $scope.changeColor (fn []
                                        (dispatch
                                          [:time-color $scope.timeColor])))

                ;; Setup re-frame subscriptions
                (def time-color (subscribe [:time-color]))
                (def timer (subscribe [:timer]))

                ;; Have angular watch the reactions for changes and update the scope variables
                ($scope.$watch (fn [] @timer)
                               (fn [newValue oldValue]
                                 (when newValue
                                        (! $scope.timeStr (-> newValue
                                                              .toTimeString
                                                              (clojure.string/split " ")
                                                              first)))))
                ($scope.$watch (fn [] @time-color)
                               (fn [newValue oldValue]
                                 (when newValue
                                             (! $scope.timeColor newValue)))))

(def.controller angularTest.AngularWatchAppDBCtrl [$scope $timeout]

                ;; $scope vars
                (! $scope.timeStr "")
                (! $scope.timeColor "#f34")

                ;; $scope functions
                (! $scope.changeColor (fn []
                                        (dispatch
                                          [:time-color $scope.timeColor])))

                ; Directly watch app-db and update the scope on change
                (add-watch app-db [:timer]
                           (fn [key atom old-state new-state]
                             ($timeout (fn []
                                         (! $scope.timeStr (-> (get-in new-state [:timer])
                                                               .toTimeString
                                                               (clojure.string/split " ")
                                                               first))))))

                ;; Watch the time color and update the scope on change
                (add-watch app-db [:time-color]
                           (fn [key atom old-state new-state]
                             ($timeout (fn []
                                         (! $scope.timeColor (get-in new-state [:time-color])))))))

(def.controller angularTest.AngularRatomRun [$scope $timeout]

                ;; $scope vars
                (! $scope.timeStr "")
                (! $scope.timeColor "#f34")

                ;; $scope functions
                (! $scope.changeColor (fn []
                                        (dispatch
                                          [:time-color $scope.timeColor])))

                ;; Functions to update the Angular scope vars
                (defn set-timer [timer]
                  ($timeout (fn []
                              (! $scope.timeStr (-> timer
                                                    .toTimeString
                                                    (clojure.string/split " ")
                                                    first)))))

                (defn set-color [color]
                  ($timeout (fn []
                              (! $scope.timeColor color))))

                ;; Setup re-frame subscriptions using ratom/run!
                (let [timer (subscribe [:timer])]
                  (reagent.ratom/run!
                    (set-timer @timer)))

                (let [time-color (subscribe [:time-color])]
                  (reagent.ratom/run!
                    (set-color @time-color)))
)


;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (dispatch-sync [:initialize])
  (reagent/render [simple-example]
                  (js/document.getElementById "reagent-app")))