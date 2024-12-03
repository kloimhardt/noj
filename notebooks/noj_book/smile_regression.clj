(ns noj-book.smile-regression 
  (:require
   [noj-book.utils.render-tools :refer [render-key-info]]
   [scicloj.kindly.v4.kind :as kind]
   [scicloj.metamorph.core :as mm]
   [scicloj.metamorph.ml :as ml]
   [scicloj.metamorph.ml.toydata :as datasets]
   [tablecloth.api :as tc]
   [tech.v3.dataset :as ds]
   [tech.v3.dataset.metamorph :as ds-mm]
   [tech.v3.datatype.functional :as dtf]))

^:kindly/hide-code
(require '[scicloj.ml.smile.regression])

;; ## Smile regression models reference
;; In the following we have a list of all model keys of Smile regression models
;; including parameters.
;; They can be used like this:

(comment
  (ml/train df
            {:model-type <model-key>
             :param-1 0
             :param-2 1}))

^:kindly/hide-code
(render-key-info :smile.regression)

