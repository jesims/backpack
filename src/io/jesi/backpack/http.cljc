(ns io.jesi.backpack.http
  #?(:cljs
     (:require-macros [io.jesi.backpack.http :refer [def-status def-status-range]])))

(defmacro def-status [status-code quoted-sym]
  (let [sym (last quoted-sym)
        status-f-name? (symbol (str sym \?))
        sym (symbol sym)]
    `(do
       (defn ~sym
         ([] (~sym {}))
         ([request#] (assoc request# :status ~status-code)))
       (defn ~status-f-name? [response#]
         (= ~status-code (:status response#))))))

(defmacro def-status-range [start end quoted-sym]
  (let [sym (last quoted-sym)
        status-f-name? (symbol (str sym \?))]
    `(defn ~status-f-name? [response#]
       (let [status# (:status response#)]
         (boolean (and status#
                       (<= ~start status# ~end)))))))

(def-status 200 'ok)
(def-status 201 'created)
(def-status 202 'accepted)
(def-status 203 'non-authoritative-information)
(def-status 204 'no-content)
(def-status 205 'reset-content)
(def-status 206 'partial-content)
(def-status 207 'multi-status)
(def-status 208 'already-reported)
(def-status 226 'im-used)

(def-status 300 'multiple-choices)
(def-status 301 'moved-permanently)
(def-status 302 'found)
(def-status 303 'see-other)
(def-status 304 'not-modified)
(def-status 305 'use-proxy)
(def-status 306 'switch-proxy)
(def-status 307 'temporary-redirect)
(def-status 308 'permanent-redirect)

(def-status 400 'bad-request)
(def-status 401 'unauthorized)
(def-status 402 'payment-required)
(def-status 403 'forbidden)
(def-status 404 'not-found)
(def-status 405 'method-not-allowed)
(def-status 406 'not-acceptable)
(def-status 407 'proxy-authentication-required)
(def-status 408 'request-timeout)
(def-status 409 'conflict)
(def-status 410 'gone)
(def-status 411 'length-required)
(def-status 412 'precondition-failed)
(def-status 413 'payload-too-large)
(def-status 414 'uri-too-long)
(def-status 415 'unsupported-media-type)
(def-status 416 'range-not-satisfiable)
(def-status 417 'expectation-failed)
(def-status 418 'iam-a-teapot)
(def-status 421 'misdirected-request)
(def-status 422 'unprocessable-entity)
(def-status 423 'locked)
(def-status 424 'failed-dependency)
(def-status 425 'too-early)
(def-status 426 'upgrade-required)
(def-status 428 'precondition-required)
(def-status 429 'too-many-requests)
(def-status 431 'request-header-fields-too-large)
(def-status 451 'unavailable-for-legal-reasons)

(def-status 500 'internal-server-error)
(def-status 501 'not-implemented)
(def-status 502 'bad-gateway)
(def-status 503 'service-unavailable)
(def-status 504 'gateway-timeout)
(def-status 505 'http-version-not-supported)
(def-status 506 'variant-also-negotiates)
(def-status 507 'insufficient-storage)
(def-status 508 'loop-detected)
(def-status 510 'not-extended)
(def-status 511 'network-authentication-required)

(def-status-range 200 299 'success)
(def-status-range 300 399 'redirection)
(def-status-range 400 499 'client-error)
(def-status-range 500 599 'server-error)
(def-status-range 400 599 'error)
