# Application Overview
This Smart Parking System uses the K-Nearest Neighbors (KNN) algorithm in order to calculate the amount of available parking spaces
using Received Signal Strength Indicator (RSSI) values.

## How it works
### Plotting the points
In this program, the K-Nearest Neighbors (KNN) algorithm is implemented by collecting the Received Signal Strength Indicator (RSSI) values
of a parking lot with a set amount of cars. This can be visualized in a table like so:

| Number of Cars | RSSI Values  |
| -------------- | ------------ |
| 0              | -30.0, -27.0 |
| 1              | -22.0, -27.0 |
| 1              | -26.0, -24.0 |
| 1              | -29.0, -25.0 |
| 1              | -32.0, -23.0 |

And this is how it can be seen in a graph:

![](https://i.imgur.com/nZFeIgk.png)

When the app is used to check how many parking spaces are still available, it compares the current RSSI values to the existing data.

Let us say for example that we used the app and got the following data:

| Number of Cars | RSSI Values  |
| -------------- | ------------ |
| ?              | -30.0, -22.0 |

And in a graph, it appears like this in accordance with the existing data:

![](https://i.imgur.com/OpzUjGu.png)

### The k-value

Now that we have our point, this is where the value of `k` comes into place. `k` is the number of neighboring points we compare to in order to
classify our point. Remember that each category is the amount of cars for that set of RSSI values.

> By default, the value of `k` in the program is 3. This means that the algorithm only compares the current point to 3 neighboring points.
> Increasing the value of `k` is better for accuracy, at the cost of speed.
> In this example, the classification of the point has changed depending on the value of the `k`:
> ![](https://helloacm.com/wp-content/uploads/2016/03/2012-10-26-knn-concept.png)

### Getting the nearest points

Now that we have our k value, it is time to get our nearest neighbors.
In order to get the distance from the current point to the neighboring points, the program uses the Euclidean Distance formula:

\\[ d(p,q) = \sqrt {\sum_{i=1}^n (q_i - p_i)^2}  \\]

where:
- \\(p, q\\) = two points in Euclidean n-space
- \\(q_i, p_i\\) = Euclidean vectors, starting from the origin of the space (initial point)
- \\(n\\) = n-space

> **NOTE:** If this looks complex, it is similar to calculating the length of the hypothenuse in a triangle (Pythagorean Theorem).
> \\[hypothenuse = \sqrt{{leg_1}^2 + {leg_2}^2}\\]
> I recommend studying more Vector maths in order to get a better understanding of Euclidean Distance.

In our program, calculating the distances can be visualized as such in accordance with the existing data:
- \\(p\\) = coordinates of the current point
- \\(q\\) = coordinates of the point of a neighbor. I will be using the amount of cars as the label for clarity.
- \\(d\\) = distance between our points

> Do take note that adding more routers would only mean adding more rows to the table,
> however it would be more difficult to visualize in a 2D graph like the ones presented in this document.

| p     | q (0) | Equation                      | d   |
| ----- | ----- | ----------------------------- | --- |
| -30.0 | -30.0 | \\(\sqrt{(-30.0 + 30.0)^2}\\) | 0   |
| -22.0 | -27.0 | \\(\sqrt{(-27.0 + 22.0)^2}\\) | 5   |

\\[\sum d(p,q)_0 = 0 + 5 = 5\\]

| p     | q (\\(1_0\\)) | Equation                      | d   |
| ----- | ------------- | ----------------------------- | --- |
| -30.0 | -22.0         | \\(\sqrt{(-22.0 + 30.0)^2}\\) | 8   |
| -22.0 | -27.0         | \\(\sqrt{(-27.0 + 22.0)^2}\\) | 5   |

\\[\sum d(p,q)_1 = 8 + 5 = 13\\]

| p     | q (\\(1_1\\)) | Equation                      | d   |
| ----- | ------------- | ----------------------------- | --- |
| -30.0 | -26.0         | \\(\sqrt{(-26.0 + 30.0)^2}\\) | 4   |
| -22.0 | -24.0         | \\(\sqrt{(-24.0 + 22.0)^2}\\) | 2   |

\\[\sum d(p,q)_2 = 4 + 2 = 6\\]

| p     | q (\\(1_2\\)) | Equation                      | d   |
| ----- | ------------- | ----------------------------- | --- |
| -30.0 | -29.0         | \\(\sqrt{(-29.0 + 30.0)^2}\\) | 1   |
| -22.0 | -25.0         | \\(\sqrt{(-25.0 + 22.0)^2}\\) | 3   |

\\[\sum d(p,q)_3 = 1 + 3 = 4\\]

| p     | q (\\(1_3\\)) | Equation                      | d   |
| ----- | ------------- | ----------------------------- | --- |
| -30.0 | -32.0         | \\(\sqrt{(-32.0 + 30.0)^2}\\) | 2   |
| -22.0 | -23.0         | \\(\sqrt{(-23.0 + 22.0)^2}\\) | 1   |

\\[\sum d(p,q)_4 = 2 + 1 = 3\\]

After we calculate the distances for each point, we can visualize them like so:

| Amount of Cars | Index | Distance |
| -------------- | ----- | -------- |
| 0              | 0     | 5        |
| 1              | 1     | 13       |
| 1              | 2     | 6        |
| 1              | 3     | 4        |
| 1              | 4     | 3        |

At this point, we only have the distances to other points, but we do not know yet the nearest neighbors.
In order to get the nearest neighbors, all we need to do is to sort the distances like so:

| Amount of Cars | Index | Distance |
| -------------- | ----- | -------- |
| 1              | 4     | 3        |
| 1              | 3     | 4        |
| 0              | 0     | 5        |
| 1              | 2     | 6        |
| 1              | 1     | 13       |

Now that the distances have been sorted, the nearest neighbors can now be gathered in accordance with the `k` value.
Since the default of `k` is 3, the nearest points would be the following:

| Amount of Cars | Index | Distance | RSSI Values  |
| -------------- | ----- | -------- | ------------ |
| 1              | 4     | 3        | -32.0, -23.0 |
| 1              | 3     | 4        | -29.0, -25.0 |
| 0              | 0     | 5        | -30.0, -27.0 |

In a graph, it can be visualized like so:
![](https://i.imgur.com/b7MSijK.png)

Because there is only 1 point that has zero cars, and 2 points that have one car,
according to the KNN algorithm, the RSSI values of the current point (-30, -22) would state that
there is 1 parking slot occupied.

![](https://i.imgur.com/O8n6Szo.png)

This means that in a parking lot of 10 slots, there is 1 occupied slot and 9 available slots.