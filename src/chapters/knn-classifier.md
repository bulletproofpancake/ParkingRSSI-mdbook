# KNN Classifier

The `KNN Classifier` class contains the logic implementing the [K-Nearest Neighbor Algorithm](https://bit.ly/2OcMqHv).
It is located at `app > java > com.silentrald.parkingrssi > KNNClassifier.kt`.
This page is for explaining the source code of how the algorithm is implemented.
For a more high level overview, please refer to the [Application Overview](applicationOverview.md).

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

### Snippet 

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

### Parameters

| Parameters | Declaration               | Description                                                       |
| ---------- | ------------------------- | ----------------------------------------------------------------- |
| point      | `point: ArrayList<Float>` | The RSSI values received from the routers.                        |
| label      | `label: Int`              | The amount of cars in the parking lot when the point is recorded. |

### Implementation

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

// matrix = {{-27.0,-36.0,-20.0,-27.0,-31.0}}

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
// matrix = {{-27.0,-36.0,-20.0,-27.0,-31.0}, -26.0,-37.0,-20.0,-28.0,-30.0}
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

### predict

### loadMatrix