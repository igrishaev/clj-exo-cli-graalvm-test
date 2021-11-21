(ns graaltest.client
  (:require
   [clojure.data.json :as json]
   )
  (:import
   java.net.URI

   java.net.http.HttpClient
   java.net.http.HttpClient$Version

   java.net.http.HttpResponse$BodyHandlers

   java.net.http.HttpHeaders
   java.net.http.HttpRequest
   java.net.http.HttpRequest$BodyPublisher
   java.net.http.HttpRequest$Builder
   java.net.http.HttpResponse
   java.net.http.HttpResponse$BodyHandler
   java.net.http.HttpResponse$BodyHandlers
   java.net.http.HttpResponse$BodySubscribers
   java.net.http.HttpResponse$BodySubscribers
   java.net.http.HttpResponse$ResponseInfo

   java.time.Duration)
  )


(defn request [{:as params
                :keys [url authorization]}]

  (let [client
        (-> (HttpClient/newBuilder) .build)

        request
        (-> (HttpRequest/newBuilder)
            (.uri (URI/create url))
            (.header "authorization" authorization)
            ;; (.header "content-type" "application/json")
            ;; (.header "accept" "application/json")
            (.build))

        response
        (.send client request (HttpResponse$BodyHandlers/ofString))]

    (-> response .body json/read-str)))
