(ns xmas-roulette.core
   (:gen-class :main true)
   (:require [postal.core :as postal])
   (:use [clojure.tools.cli :only (cli)]))

(defn do-until [f x condition]
  (if (condition x) x (recur f (f x) condition)))

(defn different-index [col-1 col-2]
  (let [index 0]
  (loop [idx index]
    (if (= (nth col-2 idx) (nth col-1 idx))
        false
        (if (= (dec (count col-1)) idx) true (recur (inc idx)))))))

(defn shuffle-emails [emails]
  "Shuffles the emails, returning a new sequence of emails that are guaranteed that each email has a new index"
  (do-until #(shuffle %) (shuffle emails) #(different-index % emails)))

(defn send-email [username password args ]
  (postal/send-message ^{:host "smtp.gmail.com"
                         :user username
                         :pass password
                         :ssl :yes}
                       {:from (str username "@gmail.com")
                        :to (:to args)
                        :subject "Secret Santa HO! HO! HO!"
                        :body (str "Merry Christmas,

You are the Secret Santa for " (:picked args) " .

To help out your Secret Santa, please create a Amazon Wish List here: http://www.amazon.com/gp/wishlist.")}))

(defn email [username password testing args]
  (doseq [i args]
         (send-email username password i)))

(defn combine [to picked]
  {:to to, :picked picked})

(defn run
  [opts args]
  (let [emails-str (slurp (:email-file opts))
        emails  (clojure.string/split-lines emails-str)
        emails-shuffled (shuffle-emails emails)]
    (email (:username opts) (:password opts) (:test opts) (map combine emails emails-shuffled))
    ))

(defn -main [& args]
  (let [[opts args banner]
        (cli args
             ["-u" "--username" "Gmail username"]
             ["-p" "--password" "Gmail password"]
             ["-e" "--email-file" "Email addresses FILE)"]
             )]
    (if
        (and
          (:username opts)
          (:password opts)
          (:email-file opts))
      (do
        (println "")
        (run opts args))
      (println banner))))
