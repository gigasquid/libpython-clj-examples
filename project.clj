(defproject gigasquid/libpython-clj-examples "0.1.0"
  :description "Example codes that use libpython-clj with various Python libraries"
  :url "https://github.com/gigasquid/libpython-clj-examples"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :jvm-opts ["-Djdk.attach.allowAttachSelf"
             "-XX:+UnlockDiagnosticVMOptions"
             "-XX:+DebugNonSafepoints"]
  :plugins [[lein-tools-deps "0.4.5"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:project]
                           :resolve-aliases []}

  :repl-options {:init-ns gigasquid.-configure})
