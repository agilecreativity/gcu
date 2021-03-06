(ns gcu.core
  (:require [cljs.core.async :as a]
            [clojure.walk :refer [keywordize-keys]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:no-doc fs (js/require "fs"))

(def ^:no-doc path (js/require "path"))

(def ^:no-doc simple-git (js/require "simple-git"))

(def git-status-chan (a/chan))

(def git-pull-chan (a/chan))

(def verbose-mode (atom false))

(defn- file-exists?
  [f]
  (fs.existsSync f))

(defn- expand-tilde
  "Expands tilde character to a path to user's home directory."
  [s]
  (clojure.string/replace-first s #"^~" (.-HOME js/process.env)))

(defn- expand-path
  [base-dir file]
  (.join path (expand-tilde base-dir) file))

(defn- dir?
  [f]
  (and
   (file-exists? f)
   (.. fs (lstatSync f) (isDirectory))))

(defn- files
  ([]
   (files "."))
  ([dir]
   (when (dir? dir)
     (seq (fs.readdirSync dir)))))

(defn- dir-exists?
  ([f]
   (dir-exists? "." f))
  ([base-dir f]
   (let [full-path (expand-path base-dir f)]
     (and (file-exists? full-path)
          (dir? full-path)))))

(defn- ensure-dir-exists
  [dir-name]
  (when-not (fs.existsSync dir-name)
    (fs.mkdirSync dir-name 0744)))

(defn- git-repo?
  ([]
   (git-repo? "."))
  ([base-dir]
   (dir-exists? base-dir ".git")))

(defn- git-projects
  "Take only list of directory that is a git project."
  [base-dir]
  (filter #(git-repo? (expand-path base-dir %)) (files base-dir)))

(defn- simple-git-instance
  "Initialize git instance for code re-use."
  ([]
   (simple-git-instance "."))
  ([directory]
   (simple-git directory)))

(defn- git-pull
  [git-instance chan git-dir]
  (.pull git-instance
         (fn [err, result]
           (if-not err
             (go (a/>! chan result))
             ;; Note: we like to know which repo, we have the problem
             (println "git pull error : " git-dir)))))

(defn- git-status
  [git-instance chan]
  (.status git-instance
           (fn [err, result]
             (if-not err
               (go (a/>! chan result))))))

(defn- all-changed-files
  "List all files in the git status as workaround to .-modified is not working!"
  [result]
  (clj->js (map #(% "path") (js->clj (.. result -files)))))

(defn- clean-status?
  "Return true if the git status is clean and ready for git pull."
  [result]
  (empty? (all-changed-files result)))

(defn- run-git-pull
  "Handle git pull command."
  [chan base-dir]
  (go
    (let [result (a/<! chan)
          updated-files (keywordize-keys (.-files result))]
      (when-not (empty? updated-files)
        (println "Changed summary : " base-dir (keywordize-keys (.. result -summary)))
        (println "Changed files   : " base-dir (keywordize-keys (.-files result)))))))

(defn- git-pull-all
  "Run git pull on all projects from a given base directory."
  [git-instance current-dir]
  (go
    (git-status git-instance git-status-chan)
    (let [result (a/<! git-status-chan)]
      (when (clean-status? result)
        (if @verbose-mode (println "git pull " current-dir))
        (git-pull git-instance git-pull-chan current-dir)

        (if @verbose-mode (println "run git pull " current-dir))
        (run-git-pull git-pull-chan current-dir)))))

;; Public API

(defn git-catch-up
  [base-dir]
  (let [dir-list (git-projects (expand-tilde base-dir))]
    (doall
     (for [current-dir dir-list]
       (go
         (if @verbose-mode (println "Process directory : " (expand-path base-dir current-dir)))
         (let [git-dir (expand-path base-dir current-dir)
               git-inst (simple-git-instance git-dir)]
           (git-pull-all git-inst git-dir)))))))

(defn -main [& args]
  (if-not (empty? args)
    (let [[base-dir & opts] args
          options (keywordize-keys (apply hash-map opts))
          {:keys [verbose] :or {:verbose "false"}} options]
      (if (= verbose "true")
        (reset! verbose-mode true)
        (reset! verbose-mode false))
      (git-catch-up base-dir))
    (println "Need to specify the base directory.")))
