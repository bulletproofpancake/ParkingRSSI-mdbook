# Application Overview

## Permissions

Before using the app, ensure that the app has the following permissions:

- Files and media
  - Allow management of all files
- Location
  - Allow only while using the app
  - Use precise location is enabled

The Files and media permissions is for allowing the app to save data to the device,
while the Location permissions are for detecting the Wi-Fi signals.

## Adding a session

Upon opening the app for the first time, you will be greeted by the `Session` page.
The `Session` page is where we can access the different configurations for training the model.

![](https://i.imgur.com/ngLLRET.jpg)

By adding a session, we can now navigate the other pages.

![](https://i.imgur.com/l8ZwO2r.gif)

## Setting up the routers

Once a session has been made, we can now visit the other pages of the app.
However, in order to use the app, the routers must first be setup in the `Routers` page.

![](https://i.imgur.com/wVnDrbz.jpg)

By default, the router matrix is set to a 2x3 configuration but this is only for visualization purposes
and has no bearing on how the results will be calculated.

In order to setup the routers:

- Adjust the router matrix
- Scan for the routers
- Select which entry in the matrix a router is to be set
- Select the appropriate router

![](https://i.imgur.com/c7LkFbS.gif)

## Recording a point

After the routers have been setup, we can now record a point by going to the `Home` page.
Put simply, a `point` is the amount of cars taken and the corresponding RSSI values at that moment.

![](https://i.imgur.com/Pu5vTez.jpg)

In the `Home` page, there are two input fields wherein numbers can be assigned.
The first one is for the **total amount of parking spaces**
and the second one is for the **total amount of occupied spaces in the current setup**.

In order to record a point:

- Enter the total amount of occupied spaces
- Press record

![](https://i.imgur.com/93kO9tl.gif)

There will be a delay before the app would say that the scan is complete,
then you can go to the `Data` page in order to view the entry.

![](https://i.imgur.com/k9FiQ30.jpg)

## Predicting the amount

Once a sufficient number of points have been recorded, the number of cars can now be predicted.

In order to predict the number of cars:

- Ensure that the routers are connected properly
- Adjust the total amount of parking spaces if necessary
- Press predict

![](https://i.imgur.com/F1uxKLh.gif)
