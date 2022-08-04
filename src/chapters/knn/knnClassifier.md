# KNN Classifier
- Contains the logic implementing the [K-Nearest Neighbor Algorithm](https://bit.ly/2OcMqHv).

# Code Analysis
## Declaration and Global Variables
```kt
{{#include ../../scripts/KNNClassifier.kt:7:10}}
```

| Snippet                                        | Description                                                                                                                                                                                                                    |
| ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `class KNNClassifier(var k: Int = 3)`          | Declares a class of `KNNClassifier` with the initial value of `k` set to `3`. `k` is the number of points that are to be evaluated before a given point is classified. ![](https://miro.medium.com/max/753/0*jqxx3-dJqFjXD6FA) |
| `var inputSize: Int = 6`                       | The number of routers to include in the algorithm. Initially set to `6`.                                                                                                                                                       |
| `val matrix = arrayListOf<ArrayList<Float>>()` | Declares a list of the values of the RSSI for each router.                                                                                                                                                                     |
| `val labels = arrayListOf<Int>()`              | Declares a list of labels for the matrix.                                                                                                                                                                                      |

## Methods
## addPoint
```kt
{{#include ../../scripts/KNNClassifier.kt:12:19}}
```

| Snippet                                            | Description                                                                        |
| -------------------------------------------------- | ---------------------------------------------------------------------------------- |
| `fun addPoint(point:ArrayList<Float>, label: Int)` | Declares a method that adds a point and label to `matrix` and `labels`.            |
| `if(point.size != inputSize) { return }`           | Exits the method if the number of points given is not the same as the `inputSize`. |
| `matrix.add(point)`                                | Adds the point to `matrix`.                                                        |
| `labels.add(label)`                                | Adds the label to `labels`.                                                        |

### Visualization
| Variables | Value                  |
| --------- | ---------------------- |
| `point`   | [ 0f, 1f, 2f, 3f, 4f ] |
| `label`   | 5                      |

Given the values of `point` and `label`, if we were to call the `addPoint` method and `matrix` and `labels` does not have any data on them, their values would be the following:

| variable | value                  |
| -------- | ---------------------- |
| `matrix` | [ 0f, 1f, 2f, 3f, 4f ] |
| `labels` | 5                      |

If we were to call the function with the same value of `point` and `label` again, the values for `matrix` and `labels` would now be:

| variable | value                                          |
| -------- | ---------------------------------------------- |
| `matrix` | [ 0f, 1f, 2f, 3f, 4f ], [ 0f, 1f, 2f, 3f, 4f ] |
| `labels` | 5, 5                                           |

## calculateDistance
```kt
{{#include ../../scripts/KNNClassifier.kt:23:31}}
```

| Snippet                                                                               | Description                                                                                                               |
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| `private fun calculateDistance(ps1: ArrayList<Float>, ps2: ArrayList<Float>) : Float` | Declares a method that calculates the distances between the list of Floats `ps1` and `ps2`, then returns a `Float` value. |
| `if (ps1.size != ps2.size) return -1f`                                                | Returns `-1f` if the amount of points given are not the same. This means that the set of points are invalid.              |
| `var distance = 0f`                                                                   | Declares a variable `distance` with the value of `0f`.                                                                    |
| `for (i in 0 until ps1.size)`                                                         | Creates a loop that ends until the size of the first list of points is reached.                                           |
| `distance += (ps1[i] - ps2[i]).pow(2)`                                                | Adds the distance to the squared difference of each point in `ps1` and `ps2`.                                             |
| `return distance`                                                                     | Returns the value of `distance`.                                                                                          |

### Visualization
| Variables | Value              |
| --------- | ------------------ |
| `ps1`     | 9f, 8f, 7f, 6f, 5f |
| `ps2`     | 0f, 1f, 2f, 3f, 4f |

Given the values of `ps1` and `ps2`, the distance that should be returned by the function is calculated like so:

| Index | Points    | Value | Distance |
| ----- | --------- | ----- | -------- |
| 0     | (9f-0f)^2 | 81f   | 81f      |
| 1     | (8f-1f)^2 | 49f   | 130f     |
| 2     | (7f-2f)^2 | 25f   | 155f     |
| 3     | (6f-3f)^2 | 9f    | 164f     |
| 4     | (5f-4f)^2 | 1f    | 165f     |

With this, the value of the distance that will be returned would be `165f`.

## predict

### Declaration

```kt
{{#include ../../scripts/KNNClassifier.kt:33}}
```
| Snippet                                     | Description                                                                      |
| ------------------------------------------- | -------------------------------------------------------------------------------- |
| `fun predict(point: ArrayList<Float>): Int` | Declares a method that returns an integer as a prediction from a list of floats. |

### Gathering the distances

```kt
{{#include ../../scripts/KNNClassifier.kt:34:39}}
```

| Snippet                                          | Description                                                                                                                                                                  |
| ------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `val distances = arrayListOf<Pair<Float,Int>>()` | Declares a list of `Float,Int` pairs that would store the distances.                                                                                                         |
| `var distance = 0f`                              | Declares a variable `distance` with the value of `0f`.                                                                                                                       |
| `for (i in 0 until matrix.size)`                 | Creates a loop that ends until the size of the `matrix` is reached.                                                                                                          |
| `distance = calculateDistance(point, matrix[i])` | Calculates the distance between the `point` and the `matrix`. Keep in mind that the values of `point` comes from the user while the values of `matrix` comes from the model. |
| `distances.add(Pair(distance,labels[i]))`        | Adds the calculated distance and pairs it to a label from `labels`.                                                                                                          |

#### Visualization

| Variables | Value              |
| --------- | ------------------ |
| `point`   | 9f, 8f, 7f, 6f, 5f |
| `matrix`  | 0f, 1f, 2f, 3f, 4f |
| `labels`  | 5, 7, 3, 4, 1      |

For the sake of simplicity, the values of `point` and `matrix` are the same as the examples of `ps1` and `ps2` as given during the visualization of the [`calculateDistance`](#calculatedistance) from earlier.
When these distances are calculated, they are each given a label which gets stored under `distances` and can be visualized like so:

| Index | Distance (`Float`) | Labels (`Int`) |
| ----- | ------------------ | -------------- |
| 0     | 81f                | 5              |
| 1     | 130f               | 7              |
| 2     | 155f               | 3              |
| 3     | 164f               | 4              |
| 4     | 165f               | 1              |

Therefore, another way of visualizing the variable `distances` would be the following:

| Variables   | Value                                                          |
| ----------- | -------------------------------------------------------------- |
| `distances` | [ 81f, 5 ], [ 130f, 7 ], [ 155f, 3 ], [ 164f, 4 ], [ 165f, 1 ] |