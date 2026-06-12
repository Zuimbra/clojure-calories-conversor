(defproject conversor "0.1.0-SNAPSHOT"
  :description "Calculadora de calorias em Clojure usando programação funcional"
  :url "https://github.com/Zuimbra/clojure-calories-conversor"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
:dependencies [[org.clojure/clojure "1.12.2"]
               [cheshire "6.2.0"]
               [compojure "1.7.2"]
               [ring/ring-jetty-adapter "1.15.4"]
               [clj-http "3.13.1"]]
  :main ^:skip-aot conversor.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})