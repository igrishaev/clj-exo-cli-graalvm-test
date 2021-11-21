(ns graaltest.sign
  "
  Signing facilities.
  "
  (:require [clojure.string :as string]
            [clojure.walk :as walk])
  (:import java.io.InputStream
           java.util.Base64
           javax.crypto.Mac
           javax.crypto.spec.SecretKeySpec))

(def scheme-token     "EXO2-TOKEN")
(def scheme-signature "EXO2-HMAC-SHA256")


(defn- ^"[B" string->bytes
  [^String s]
  (.getBytes s "UTF-8"))


(defn- ^String b->b64
  "Convert a byte-array to base64"
  [^bytes b]
  (String. (.encode (Base64/getEncoder) b)))


(defn ^String b64->str
  "Convert base64 to string"
  [^String s]
  (String. (.decode (Base64/getDecoder) s)))


(defn- factory
  "Builds a function to compute the HMAC of a payload, given a secret-key.
   `hmac-type` is the algorithm to use for the HMAC."
  [^String hmac-type input-transformer key-transformer output-transformer]
  (fn [secret-string ^String raw-input]
    (let [secret ^"[B" (key-transformer secret-string)
          input  ^"[B" (input-transformer raw-input)
          key    (SecretKeySpec. secret hmac-type)]
      (-> (doto (Mac/getInstance hmac-type) (.init key))
          (.doFinal input)
          (output-transformer)))))


(def sha256 (factory "HmacSHA256"
                     string->bytes
                     string->bytes
                     b->b64))

(defn- ->payload
  [provided needed]
  (reduce str "" (for [n needed]
                   (get provided n))))


(def stream? (partial instance? InputStream))


(defn body->string
  "
  Turn the request's body into a string to sign. Note that
  the body might be of a different type depending on the current
  step in a chain of interceptors. When the body is a stream,
  it might have been already read, so slurping it would give
  an empty string. But if it's a ByteArrayInputStream, we may
  reset it to the beginning.
  "
  [body]
  (cond
    (string? body) body

    (nil? body) ""

    (stream? body)
    (do (when (.markSupported ^InputStream body)
          (.reset ^InputStream body))
        (slurp body))

    :else
    (throw (new Exception (format "Wrong body type: %s" (type body))))))


(defn build-payload [{:keys [query-params headers
                             request-method uri body]}
                     {:keys [signed-query-args
                             signed-headers
                             expires]}]
  (let [params  (->payload query-params signed-query-args)
        headers (-> headers
                    walk/keywordize-keys
                    (->payload signed-headers))
        method  (some-> request-method name string/upper-case)
        expires (or expires "")]
    (str method " " uri "\n" (body->string body) "\n" params  "\n" headers "\n" expires)))


(defn signature
  [{:keys [secret-key
           request-method
           uri
           query-params
           headers
           body
           expires]}]

  (let [method (-> request-method
                   name
                   string/upper-case)
        payload (build-payload
                 {:request-method method
                  :uri uri
                  :query-params query-params
                  :headers headers
                  :body body}
                 {:signed-query-args (keys query-params)
                  :signed-headers (keys headers)
                  :expires expires})]
    (sha256 secret-key payload)))


(defn sig-auth-header
  [{:keys [access-key
           query-params
           headers
           expires]
    :as request}]

  (let [signed-query-args
        (when-let [qa (not-empty (string/join ";" (map name (keys query-params))))]
          (str "signed-query-args=" qa ","))

        signed-headers
        (when-let [sh (not-empty (string/join ";" (map name (keys headers))))]
          (str "signed-headers=" sh ","))

        expires
        (when expires
          (str "expires=" expires ","))

        signature
        (str "signature=" (signature request))]

    (str scheme-signature " credential=" access-key ","
         signed-query-args
         signed-headers
         expires
         signature)))


(defn sign-request [request]
  (let [header (sig-auth-header request)]
    (assoc-in request [:headers :authorization] header)))
