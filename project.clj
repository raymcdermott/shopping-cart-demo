(defproject shopping-cart-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                   :username :env
                                   :password :env}}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [environ "1.0.1"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [prismatic/schema "1.0.3"]
                 [com.datomic/datomic-pro "0.9.5344" :exclusions [joda-time]]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "shopping-cart-demo-standalone.jar"
  :profiles {:production {:env {:production true}}})
