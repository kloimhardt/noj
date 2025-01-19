;; # Advanced Table Processing with Tablecloth - draft 🛠

;; authors: Cvetomir Dimov and Daniel Slutsky

;; last change: 2025-01-19

;; ## Setup

(ns noj-book.tablecloth-advanced-table-processing
  (:require [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.dataset.print :as print]
            [clojure.string :as str]
            [scicloj.kindly.v4.kind :as kind]
            [java-time.api :as java-time]
            [tech.v3.datatype.datetime :as datetime]))

;; ## Pivoting

;; The following is inpired by [R4DS](https://r4ds.had.co.nz) [Tidy data / Pivoting](https://r4ds.had.co.nz/tidy-data.html#pivoting>).

;; Pivoting datasets means changing their shape - either splitting a culumn into several or putting the data from two or more columns into one. There are two functions that perform these transformations, respectively: pivot->longer and pivot-wider. Here we will demonstrate how they work.

;; We will use the dataset "cases-data", which corresponds to table4a in the aforementioned chapter of R4DS, for this demontration.

(def cases-data
  (tc/dataset
   [["Aghanistan" 745 2666]
    ["Brazil" 37737 80488]
    ["China" 212258 213766]]
   {:column-names [:country 1999 2000]}))


;; The first dataset, "cases-data", stores the number of cases in the population of a country in two different years. The data for each year are stored in a separate column.

cases-data

;; "pivot->longer" places the data from two or more columns into one column, and their column names into another. In this case, we would like to transform 'cases-data' by placing the data from columns '1999' and '2000' into a single column, called :cases, and also placing the column names into a new column called :year. This can be done by providing a list of all columns that will be pivoted. 

(-> cases-data
    (tc/pivot->longer [1999 2000]
                      {:target-columns :year
                       :value-column-name :cases}))

;; Alternatively one can provide a predicate that select the columns whose name agrees with the predicate. 

(-> cases-data
    (tc/pivot->longer (fn [column-name]
                        (not= column-name :country))
                      {:target-columns :year
                       :value-column-name :cases}))

;; A more clojure-y way of writing the same predicate would use a set of column names. We can also order the resulting data. We will save the result to demonstrate pivot->wider.

(def cases-longer
  (-> cases-data
      (tc/pivot->longer (complement #{:country})
                        {:target-columns :year
                         :value-column-name :cases})
      (tc/order-by [:country :year])))

;; For more examples of pivot->longer, you can refer to [tablecloth's pivot longerdocumentation](https://scicloj.github.io/tablecloth/#longer). 

;; We can think of pivot->wider as the reverse operation of pivot->longer: it spreads data into more columns. It needs as input a column (or columns) that holds the column names of the new dataset and another one that holds the value. We can return to our initial dataset like this. 

(-> cases-longer
    (tc/pivot->wider [:year]
                     [:cases]))

;; For additional examples with pivot-wider, you can refer to [tablecloth's pivot wider documentation](https://scicloj.github.io/tablecloth/#wider). 

;; ## Joining
;; The following is inpired by [R4DS](https://r4ds.had.co.nz) [Relational data / Mutating joins](https://r4ds.had.co.nz/relational-data.html#mutating-joins).

;; Joining datasets means putting them together. When we join, we specify one or more columns that they have in common and we would like them to share. The result of the join is a new dataset that has these shared columns and all other columns, aligned by the shared columns. 

;; To demonstrate joins, we will use the dataset "population-data" in addition to "cases-data". The former corresponds to table4b in the chapter 12 of R4DS. It stores the population of a country for different years. The data for different years are in different columns. 

(def population-data
  (tc/dataset
   [["Aghanistan" 19987071 20595360]
    ["Brazil" 172006362 174504898]
    ["China" 1272915272 1280428583]]
   {:column-names [:country 1999 2000]}))

;; For our demonstration, we will also need to pivot that dataset longer. 

(def population-longer
  (-> population-data
      (tc/pivot->longer (complement #{:country})
                        {:target-columns :year
                         :value-column-name :population})
      (tc/order-by [:country :year])))

population-longer

(tc/left-join
 cases-longer
 population-longer
 [:country :year])

;; The new dataset holds all the columns from the old datasets. In its current implemenation, the left-join operation also copies the columns that we join over for the right dataset. This is done so that this information is not lost. In our example, the two datasets have the exact same values for the column that we join over. This is however not always the case, because of which these columns might be useful. If we would like to drop that information, we can simply select the columns that interest us. Note that we need to order the dataset after this transformation. 

(def population-and-cases
  (-> (tc/left-join
       cases-longer
       population-longer
       [:country :year])
      (tc/select-columns [:country :year :cases :population])
      (tc/order-by [:country :year])))

population-and-cases

;; Note that we have only demonstrated a left join. When doing the left join, the first data set remains unchanged and the second dataset is adapted accordingly. For example, if the second dataset misses some of the column values for the columns that we join over, we will have empty values. If it has additional column values, those will be dropped. The converse is true for a right join (function right-join) - the second dataset well remain unchanged. In addition, one can perform an inner joint, in which all rows not shared among the data sets are dropped, and a full join, in which no row is dropped. 

;; For addditional examples of joins, refer to [tablecloth's join/concat datasets documentation](https://scicloj.github.io/tablecloth/#joinconcat-datasets). 

;; ## Joining and separating columns

;; The following is inpired by [R4DS](https://r4ds.had.co.nz) [Tidy data / Separating and uniting](https://r4ds.had.co.nz/tidy-data.html#separating-and-uniting).

;; Two other operations that are commonly performed are joining the values of two columns into a new column and separating one column into two new columns. 

(tc/join-columns population-and-cases
                 :ratio [:cases :population])

;; We can use any separator that we wans. In this case, we will use a divisor "/", because we compute a ratio. We will save this variable for later.

(def ratio-data
  (tc/join-columns population-and-cases
                   :ratio [:cases :population]
                   {:separator "/"}))

ratio-data

;; The reverse operation is "separate-column". We can split the new dataset back into the old one.

(tc/separate-column ratio-data :ratio [:cases :population] "/")

;; For addditional examples of column joins and separations, refer to [tablecloth's join/separate columns documentation](https://scicloj.github.io/tablecloth/#joinseparate-columns)
