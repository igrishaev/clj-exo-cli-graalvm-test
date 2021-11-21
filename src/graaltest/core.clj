(ns graaltest.core
  (:gen-class)
  (:require
   [graaltest.sign :as sign]
   [clojure.java.io :as io]

   #_
   [clj-http.client :as client]

   ;; [clj-http.lite.client :as client]

   [graaltest.client :as client2]

   ;; [hato.client :as hc]
   [clojure.pprint :as pprint]

   [clojure.data.json :as json]
   ;; [cheshire.core :as json]

   ))


(def api-spec
  (-> "public-api.json"
      io/resource slurp
      json/read-str))


(def command->api

  (let [{:strs [paths]}
        api-spec

        result
        (atom nil)]

    (doseq [[path method->api] paths
            [method api] method->api

            :let [{:strs [operationId]}
                  api]]

      (swap! result assoc operationId {:method method
                                       :path path
                                       :api api}))

    @result))


#_
(def command->api
  (reduce-kv
   (fn [result path method->api]
     (merge result
            (reduce-kv
             (fn [result method {:as api :strs [operationId]}]
               (assoc result operationId {:method method
                                          :path path
                                          :api api}))
             method->api
             result)))
   {}
   (get api-spec "paths")))

#_
(clojure.inspector/inspect-tree command->api)


(defn pp-request
  [{:keys [path-prefix access-key secret-key]}
   request-method
   {:keys [uri query-params headers body]}]

  (let [body (when body
               (json/write-str body))

        authorization (sign/sig-auth-header
                       {:access-key access-key
                        :secret-key secret-key
                        :request-method request-method
                        :uri (str path-prefix uri)
                        :query-params query-params
                        :headers headers
                        :body body})

        url (str "https://ppapi-ch-gva-2.exoscale.com" path-prefix uri)
        ;; url (str "https://ppapi-ch-gva-2.exoscale.com/v2.alpha" uri)

        options {:url              url
                 :query-params     (or query-params {})
                 :method           request-method
                 :throw-exceptions false
                 ;; :as               :json
                 ;; :coerce           :always

                 :headers {"authorization" authorization}

                 ;; :headers          (merge {:authorization authorization}
                 ;;                          (when body
                 ;;                            {:content-type "application/json"})
                 ;;                          headers)

                 :body body}]

    (client2/request {:url url :authorization authorization})

    #_
    (-> (client/request options)
        :body
        json/read-str)))

#_
(pp-request
 {:path-prefix "/v2.alpha"
  :access-key "EXO580fde0cd7a3e284644b999b"
  :secret-key "tr_Gd5wpRd2CwY3Ep3M0cmdb5K8U-5AIJvoEjaQ9RY4"}
 "get"
 {:uri "/zone"})


(def enumerate
  (partial map-indexed vector))


(defn replace-params [path params values]
  (loop [path path
         iparams (enumerate params)]
    (let [[iparam & iparams] iparams]
      (if iparam
        (let [[i param] iparam
              param-name (get param "name")
              param-value (get values i :not-found)

              _
              (when (= :not-found param-value)
                (throw (new Exception (format "no value for param %s" param-name))))

              path
              (.replace ^String path ^String (str "{" param-name "}") ^String (str param-value))]

          (recur path iparams))
        path))))


(defn exec-command [command args]

  (when-let [{:as found
              :keys [method path api]}
             (get command->api command)]

    (let [{:strs [parameters]}
          api

          ;; _
          ;; (clojure.inspector/inspect-tree parameters)

          url
          (if (seq parameters)
            (replace-params path parameters args)
            path)]

      (pp-request
       {:path-prefix "/v2.alpha"
        :access-key (or (System/getenv "ACCESS_KEY") (throw (new Exception "ACCESS_KEY not set")))
        :secret-key (or (System/getenv "SECRET_KEY") (throw (new Exception "SECRET_KEY not set")))}
       method
       {:uri url}))))


(defn -main
  "I don't do a whole lot ... yet."
  [& [command & args]]

  (pprint/pprint (exec-command command (vec args)))

  #_
  (let [response
        (client/get "https://cat-fact.herokuapp.com/facts")]
    (println
     response

     #_
     (json/generate-string
      (:body response)
      {:pretty true}))))
