# KNN Classifier

The `KNN Classifier` class contains the logic implementing the [K-Nearest Neighbor Algorithm](https://bit.ly/2OcMqHv).
It is located at `app > java > com.silentrald.parkingrssi > KNNClassifier.kt`.
This page is for explaining the source code of how the algorithm is implemented.
For a more high level overview, please refer to the [Algorithm Overview](algorithmOverview.md).

## Fields 

The `KNN Classifier` class has multiple fields which are needed in order to calculate the number of available parking spaces
using the algorithm.

| Field     | Declaration                                    | Description                                                                                 |
| --------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------- |
| k         | `var k: Int = 3`                               | The number of neighbors to be evaluated by the algorithm. The value is set to 3 by default. |
| inputSize | `var inputSize: Int = 6`                       | The number of routers to get RSSI values from.                                              |
| matrix    | `val matrix = arrayListOf<ArrayList<Float>>()` | A collection of RSSI values gathered from the routers.                                      |
| labels    | `val labels = arrayListOf<Int>()`              | A collection of the amount of cars for a set of RSSI values.                                |

## Methods

### addPoint

The `addPoint` method adds a point to the dataset. This point contains the RSSI values and the amount of cars for that set.

#### Snippet 

```kt
fun addPoint(point: ArrayList<Float>, label: Int) {
    if (point.size != inputSize) {
      return
    }

    matrix.add(point)
    labels.add(label)

    k = sqrt(matrix.size.toDouble()).toInt()
  }
```

#### Parameters

| Parameters | Declaration               | Description                                                       |
| ---------- | ------------------------- | ----------------------------------------------------------------- |
| point      | `point: ArrayList<Float>` | The RSSI values received from the routers.                        |
| label      | `label: Int`              | The amount of cars in the parking lot when the point is recorded. |

#### Implementation

At the start of the method, the size of the point is compared to the `inputSize`.
Each router sends a single RSSI value which is contained as a list in the `point` variable.
This check then is to ensure that all routers have sent an RSSI value before it is added to the database.

```kt
// For example:
// point = {-27.0,-36.0,-20.0,-27.0,-31.0}

// Given that there are 5 entries in point:
// point.size = 5

// inputSize is based on the amount of routers selected.
// Given that each router sends a single RSSI value:
// inputSize = 5

// Because the point.size and inputSize is the same
// the method would not return and continue to the next step

if (point.size != inputSize){
    return
}
```

If the `point` entered is valid, it gets added to the `matrix` and the `label` gets added to the `labels`.
In essence, this "pairs" the `label` with the `point` entered as they share the same index as they get added to their lists at the same time.

```kt
// For example:
// point = {-27.0,-36.0,-20.0,-27.0,-31.0}
// label = 0
// matrix = {}
// labels = {}

// The reason matrix is empty is because there are no points added to it yet.
// labels is empty for the same reason.

matrix.add(point)

// matrix = [{-27.0,-36.0,-20.0,-27.0,-31.0}]

// After adding the point, the matrix now contains the list of RSSI values from the point.
// This is why in the declaration of the matrix, it is an arrayListof<ArrayList<Float>>()
// The matrix is a list for a list of float variables.

labels.add(label)

// labels = {0}

// After adding the label, the labels list now contain the value of the label.
```

Once the `point` and `label` has been added to the database, the value of `k` is adjusted.

```kt
// For example, another point has been added to the matrix:
// matrix = [{-27.0,-36.0,-20.0,-27.0,-31.0}, -26.0,-37.0,-20.0,-28.0,-30.0}]
// matrix.size = 2
// matrix.size.toDouble = 2.0
// sqrt(matrix.size.toDouble()) = 1.41421356237309511.0
// sqrt(matrix.size.toDouble()).toInt() = 1

k = sqrt(matrix.size.toDouble()).toInt()

// By doing this, the points gets compared to more neighbors as it increases.
```

As mentioned before, this "pairs" the `point` and `label` together.
For example, the data collected from five routers can be presented in a table like so:

| label | 66852627208993-xiaomi-repeater-v3_miapdb21 | 66852627204643-xiaomi-repeater-v3_miapca23 | 66852627209260-xiaomi-repeater-v3_miapdc2c | 66852627198541-xiaomi-repeater-v3_miapb24d | 66852627208795-xiaomi-repeater-v3_miapda5b |
| ----- | ------------------------------------------ | ------------------------------------------ | ------------------------------------------ | ------------------------------------------ | ------------------------------------------ |
| 0     | -27.0                                      | -36.0                                      | -20.0                                      | -27.0                                      | -31.0                                      |
| 1     | -26.0                                      | -37.0                                      | -20.0                                      | -28.0                                      | -30.0                                      |
| 2     | -25.0                                      | -43.0                                      | -18.0                                      | -29.0                                      | -31.0                                      |
| 3     | -26.0                                      | -39.0                                      | -21.0                                      | -27.0                                      | -32.0                                      |
| 4     | -26.0                                      | -39.0                                      | -24.0                                      | -25.0                                      | -31.0                                      |
| 5     | -25.0                                      | -41.0                                      | -25.0                                      | -23.0                                      | -31.0                                      |

However, in the code these data are stored in two different lists, the `matrix` and `labels`, and they can be represented like so:

```json
[
  {
    "labels": "{0,1,2,3,4,5}",
    "matrix": "{-27.0,-36.0,-20.0,-27.0,-31.0},{-26.0,-37.0,-20.0,-28.0,-30.0},{-25.0,-43.0,-18.0,-29.0,-31.0},{-26.0,-39.0,-21.0,-27.0,-32.0},{-26.0,-39.0,-24.0,-25.0,-31.0},{-25.0,-41.0,-25.0,-23.0,-31.0}"
  }
]
```
Or in a table like so:

| labels | matrix                          |
| ------ | ------------------------------- |
| 0      | {-27.0,-36.0,-20.0,-27.0,-31.0} |
| 1      | {-26.0,-37.0,-20.0,-28.0,-30.0} |
| 2      | {-25.0,-43.0,-18.0,-29.0,-31.0} |
| 3      | {-26.0,-39.0,-21.0,-27.0,-32.0} |
| 4      | {-26.0,-39.0,-24.0,-25.0,-31.0} |
| 5      | {-25.0,-41.0,-25.0,-23.0,-31.0} |

### calculateDistance

The `calculateDistance` method aggregates the distance between two collections of RSSI values.
In the app, one collection is from the current RSSI values received by the device
and another is from the existing RSSI values added to the `matrix`.
This is because the `calculateDistance` method is used when the app is [predicting](#predict).

It helps to think of each collection of RSSI values as a coordinate, however as the number of routers increase
so too does the dimensions in which these coordinates exist, which is why the Euclidean Distance formula is used to
calculate the distance between each point.

> **NOTE:**
> For a further in-depth breakdown of the usage of the Euclidean Distance formula,
> please refer to the [Getting the nearest points](algorithmOverview.md#getting-the-nearest-points)
> section in the Algorithm Overview.

#### Snippet

```kt
private fun calculateDistance(ps1: ArrayList<Float>, ps2: ArrayList<Float>): Float {
    if (ps1.size != ps2.size) return -1f

    var distance = 0f
    for (i in 0 until ps1.size) {
      distance += (ps1[i] - ps2[i]).pow(2) // (x1 - x2) ^ 2
    }
    return distance
  }
```

#### Parameters

| Parameters | Declaration             | Description                                           |
| ---------- | ----------------------- | ----------------------------------------------------- |
| ps1        | `ps1: ArrayList<Float>` | The current RSSI values being detected by the device. |
| ps2        | `ps2: ArrayList<Float>` | The RSSI values from the `matrix`.                    |

#### Returns

| Returns  | Type    | Description                                                         |
| -------- | ------- | ------------------------------------------------------------------- |
| distance | `Float` | The aggregated distance between the two collections of RSSI values. |

#### Implementation

```kt
// Compare the sizes of the parameters to check if it is valid.
// Returning a negative distance would mean that the parameters are invalid.
if(ps1.size != ps2.size) return -1f

// The next line creates a variable distance.
// This is where the aggregated distances will be stored and returned.
var distance = 0f

// The next line states that until the index reaches the size of ps1,
// the Euclidean Distance of the two points would be added to distance.
for (i in 0 until ps1.size){
    distance += (ps1[i] - ps2[i]).pow(2)
}

// Once the loop is finished, the distance would be returned.
return distance
```

Let us say for example that we are working with the following data:

| i   | ps1   | ps2   |
| --- | ----- | ----- |
| 0   | -27.0 | -26.0 |
| 2   | -36.0 | -37.0 |
| 2   | -20.0 | -20.0 |
| 3   | -27.0 | -28.0 |
| 4   | -31.0 | -30.0 |

When we use the function, we first check if the size of both lists are the same.

```kt
if(ps1.size != ps2.size) return -1f
```

This means that if we use the data given above, we would not return `-1f` because both lists have 5 items in them,
however if ps1 or ps2 does not have the same number of items, we would return `-1f` and we would not calculate the distances between those points
because the given list of points are invalid.
The reason we are returning `-1f` is because the function requires a number to be returned which is the `distance`, therefore, if we return a negative distance,
this means that there has been an error in processing the given points.

Afterwards, the distance is computed by going through each item in the lists and aggregating their distances.

| i   | ps1   | ps2   | distance | \\[\sum_{i=0}^{ps1.size} {distance}\\] |
| --- | ----- | ----- | -------- | -------------------------------------- |
| 0   | -27.0 | -26.0 | 1        | 1                                      |
| 2   | -36.0 | -37.0 | 1        | 2                                      |
| 2   | -20.0 | -20.0 | 0        | 2                                      |
| 3   | -27.0 | -28.0 | 1        | 3                                      |
| 4   | -31.0 | -30.0 | 1        | 4                                      |

After this loop has been finished, we have arrived at the distance of `4`,
which means the calculated distance that will be returned form the function will be `4`.

### predict

The `predict` method is where the actual implementation of the K-Nearest Neighbor algorithm as it predicts
the amount of cars taken with the `point` given to it.

#### Snippet

```kt
fun predict(point: ArrayList<Float>): Int {
    val distances = arrayListOf<Pair<Float, Int>>()
    for (i in 0 until matrix.size) {
      distances.add(Pair(calculateDistance(point, matrix[i]), labels[i]))
    }

    distances.sortWith(compareBy { it.first })
    Log.i("KNN", distances.toString())

    val hashmap = hashMapOf<Int, Int>()

    var end = k.coerceAtMost(matrix.size)
    for (i in 0 until end) {
      val classification = distances[i].second
      hashmap[classification] = hashmap.getOrDefault(classification, 0) + 1
    }

    // Get Max (Plurality)
    var max = -1
    val outputs = arrayListOf<Int>()
    for ((key, value) in hashmap) {
      if (value > max) {
        max = value
        outputs.clear()
        outputs.add(key)
      } else if (value == max) {
        outputs.add(key)
      }
    }

    if (outputs.size == 1) {
      return outputs[0]
    }

    // Tie-breaking
    while (end > 0) {
      end--
      val classification = distances[end].second
      hashmap[classification]?.dec()

      max = -1
      outputs.clear()
      for ((key, value) in hashmap) {
        if (value > max) {
          max = value
          outputs.clear()
          outputs.add(key)
        } else if (value == max) {
          outputs.add(key)
        }
      }

      if (outputs.size == 1) {
        return outputs[0]
      }
    }

    // Just return the first one
    return distances[0].second
  }
```

#### Parameters

| Parameters | Declaration        | Description                                           |
| ---------- | ------------------ | ----------------------------------------------------- |
| point      | `ArrayList<Float>` | The current RSSI values being detected by the device. |

#### Returns

| Returns             | Type  | Description                                         |
| ------------------- | ----- | --------------------------------------------------- |
| outputs[0]          | `Int` | The largest `label` recorded in the database.       |
| distances[0].second | `Int` | The `label` of the nearest neighbor to the `point`. |

#### Implementation

At the start of the `predict` method, a List of a [`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/)
of types `Float` and `Int` are created and stored in the variable `distances`.
The `Float` is responsible for containing the distance from the `point` to the existing points added in the database,
while the `Int` contains the amount of cars expected for the point according to the database.

```kt
val distances = arrayListOf<Pair<Float, Int>>()

for (i in 0 until matrix.size){
  distances.add(Pair(calculateDistance(point,matrix[i]), labels[i]))
}
```

For example, the given data is the following:

```
label = {2,3,4}
matrix = [
  {-25.0,-43.0,-18.0,-29.0,-31.0},
  {-26.0,-39.0,-21.0,-27.0,-32.0},
  {-26.0,-39.0,-24.0,-25.0,-31.0}
  ]
point = {-26.0,-35.0,-21.0,-26.0,-27.0}
```
When graphed into a table, it would look like the following:

| i   | labels[i] | matrix[i]                     | point                         |
| --- | --------- | ----------------------------- | ----------------------------- |
| 0   | 2         | -25.0,-43.0,-18.0,-29.0,-31.0 | -26.0,-35.0,-21.0,-26.0,-27.0 |
| 1   | 3         | -26.0,-39.0,-21.0,-27.0,-32.0 | -26.0,-35.0,-21.0,-26.0,-27.0 |
| 2   | 4         | -26.0,-39.0,-24.0,-25.0,-31.0 | -26.0,-35.0,-21.0,-26.0,-27.0 |

When the distances are calculated and paired, we get the following result:

| i   | Pair(calculateDistance,labels[i]) |
| --- | --------------------------------- |
| 0   | (98.0, 2)                         |
| 1   | (41.0, 3)                         |
| 2   | (41.0, 4)                         |

After calculating the `distances`, it is sorted according to the calculated distance which is the `first` item in the pair.

```kt
distances.sortWith(compareBy {it.first})
```

Once sorted, the `distances` is now the following:

| i   | Pair(calculateDistance,labels[i]) |
| --- | --------------------------------- |
| 0   | (41.0, 3)                         |
| 1   | (41.0, 4)                         |
| 2   | (98.0, 2)                         |

After sorting the `distances`, a [`hashmap`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-hash-map/)
is created in order to store the amount of neighbors for each classification (the number of cars or label).

For example:

> **NOTE:** Additional data has been added in order to provide a more clear example.

| i   | distances | label |
| --- | --------- | ----- |
| 0   | 35.0      | 3     |
| 1   | 41.0      | 3     |
| 2   | 41.0      | 4     |
| 3   | 98.0      | 2     |

When the data is stored into the hashmap, it can be seen like so:

```kt
val hashmap = hashMapOf<Int, Int>()
// hashmap = {}

var end = k.coerceAtMost(matrix.size)
// matrix.size = 4
// since k = sqrt(matrix.size.toDouble()).toInt()
// k = 2 for this example
// therefore, end = 2

for (i in 0 until end){
  val classification = distances[i].second
  // In the pair, distance[0] = (35.0, 3)
  // By getting the second value in the pair
  // classification = 3

  hashmap[classification] = hashmap.getOrDefault(classification, 0)+1
  // hashmaps are similar to pairs, but they function differently.
  // hashmaps are key, value pairs
  // wherein the first element is the key and the second element is the value.
  // keys can be used as an index and they can store a value isolated from the items in the hashmap

  // in the line above, hashmap[classification] sets a value that gets paired to the classification
  // which in the case of the current i (0) is 3
  // So when it is visualized
  // hashmap = {3=null}
  // when hashmap.getOrDefault(classification, 0)+1 gets called
  // the value paired(null) to the classification(3) either gets set to
  // a number or set to the default value of 0 then incremented by 1
  // since the current value is null, it is set to 0 then incremented by 1
  // so after the first iteration hashmap = {3=1}

  // since this is in a loop until the end(2) is reached
  // the next iteration has the current values:
  // distance[1] = (41.0, 3)
  // classification = 3
  
  // now when hashmap.getOrDefault is ran,
  // hashmap[3] is already equal to 1 due to the previous iteration
  // so in this iteration, the value of hashmap[3] is incremented
  // hashmap = {3=2}
}
```

Due to the limitation set on the `k` value, the algorithm only had a single classification,
but if we are to go through all of the labels in `distances`, the hashmap would have the following value

```kt
hashmap = {2=1, 3=2, 4=1}
```

These means that for the current point,
it has one neighbor classified as 2,
two neighbors classified as 3,
and one neighbor classified as 4.

After classifying the neighbors, the most number of neighbors of the same classification must be returned,
which is retrieved by the following lines of code:

```kt
// set to -1 as the values of the hashmap
// would always be greater than this
var max = -1
// created to contain the classification
// of the greatest amount of neighbors
val outputs = arrayListOf<Int>()
for ((key, value) in hashmap) {
  if (value > max) {
    max = value
    outputs.clear()
    outputs.add(key)
  } else if (value == max) {
    outputs.add(key)
  }
}
// The key is the one that gets added to the outputs
// as it is the classification which in the context of the application
// is the amount of cars taken

if (outputs.size == 1) {
  return outputs[0]
  // Given the examples above,
  // the output that gets returned is 3
  // which means that for that set of RSSI values
  // there are 3 parking spaces occupied
}
```
 
However, it could be possible for the neighbors to have different classifications,
yet the same amount of points.

For example:
hashmap = {2=1, 3=2, 4=1}

Which is why a tie-breaker is implemented like so:

```kt
while (end > 0) {
  end--
  val classification = distances[end].second
  // Decreases the value stored in that classification
  hashmap[classification]?.dec()
  // hashmap = {2=0, 3=1, 4=0}
  // this means that 3 is still the classification that gets returned
  // as it is only classification with the highest value

  max = -1
  outputs.clear()
  for ((key, value) in hashmap) {
    if (value > max) {
      max = value
      outputs.clear()
      outputs.add(key)
    } else if (value == max) {
      outputs.add(key)
    }
  }

  if (outputs.size == 1) {
    return outputs[0]
  }
}
```

And if, for some reason all of the neighbors are all the same value like so:
hashmap = {2=1, 3=2, 4=1}

Then the algorithm would return the classification of the nearest neighbor:

```kt
return distances[0].second
// distances[0] = (35.0, 3)
// which means that the classification still returned is 3
```

### loadMatrix

The `loadMatrix` method loads the matrix from an existing database.

#### Snippet

```kt
fun loadMatrix(data: ArrayList<RowData>) {
    inputSize = data.size

    labels.clear()
    matrix.clear()
    for (row in data) {
      labels.add(row.label)
      matrix.add(row.values)
    }

    k = sqrt(matrix.size.toDouble()).toInt()
  }
```

#### Parameters

| Parameter | Declaration                | Description                                                         |
| --------- | -------------------------- | ------------------------------------------------------------------- |
| data      | `data: ArrayList<RowData>` | A list of [RowData](row-data.md) which the matrix gets loaded from. |

#### Implementation

The start of the method overwrites the `inputSize` with the size of the `data` and clears the existing values of `labels` and `matrix`.

```kt
inputSize = data.size

labels.clear()
matrix.clear()
```

After clearing the existing values in the database, the values from `data` gets added into the `labels` and `matrix` lists.

```kt
// The same as:
// for (i in 0 until data.size){
//     var row = data[i]
//     labels.add(row.label)
//     matrix.add(row.values)
// }
for (row in data){
    labels.add(row.label)
    matrix.add(row.values)
}
```

Once the `labels` and `matrix` has been loaded from the `data`, the `k` value gets updated as well.

```kt
k = sqrt(matrix.size.toDouble()).toInt()
```