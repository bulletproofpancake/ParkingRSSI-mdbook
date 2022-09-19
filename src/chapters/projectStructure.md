# Project Structure

The project consists of two major components, the source code and the layout files.
Source code files end in `.kt` while layout files end in `.xml`.
Source code files are responsible for the logic of the app
while layout files are responsible for the interface.

## Source Code Directories

When the project is opened in Android Studio, there is a file explorer located in the left portion of the screen by default named `Project`.
Ensure that `Android` is selected on the tab on the top as it is a simple layout to navigate.
Under `app`, expand the `java` folder, and `com.silentrald.parkingrssi`.
All the files inside this folder contains the source code for the project.

![](https://i.imgur.com/gmHlPxZ.gif)

### Descriptions

| Folder         | File                | Description                                                                                                                                                                            |
| -------------- | ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **interfaces** |                     | Contains [interfaces](https://kotlinlang.org/docs/interfaces.html) which will be used by the other classes.                                                                            |
|                | CellClickListener   |                                                                                                                                                                                        |
| **models**     |                     | Contains the [classes](https://kotlinlang.org/docs/classes.html) needed by the program to create and train the model for the neural network.                                           |
|                | DBHelper            | Contains methods in order to create an [`SQLite Database`](https://www.geeksforgeeks.org/android-sqlite-database-in-kotlin/) for recording information about the routers and sessions. |
|                | Prefs               | Contains information to save the session and capacity to the device.                                                                                                                   |
|                | RowData             | Contains the number of cars occupied for a set of RSSI values.                                                                                                                         |
|                | Session             | Contains the setup of how the model is trained.                                                                                                                                        |
| **views**      |                     | Contains the classes to interact with the different screens of the application.                                                                                                        |
|                | DataFormDialog      |                                                                                                                                                                                        |
|                | DataFragment        |                                                                                                                                                                                        |
|                | HomeFragment        |                                                                                                                                                                                        |
|                | RecyclerViewAdapter |                                                                                                                                                                                        |
|                | ScanRouterFragment  |                                                                                                                                                                                        |
|                | SessionFragment     |                                                                                                                                                                                        |
| **-**          |                     | The classes found on the root of `com.silentrald.parkingrssi`                                                                                                                          |
|                | KNNClassifier       | Contains the logic implementing the [K-Nearest Neighbor Algorithm](https://bit.ly/2OcMqHv).                                                                                            |
|                | MainActivity        | Controls the navigation of the program.                                                                                                                                                |
|                | Router              | Contains methods in order for the program to view and record routers.                                                                                                                  |

## Layout Directories

The layout directories contain the files that create the user interface of the application.
It also contains the ids of each component which would be used by the scripts in the `views` directory.
The layouts are stored in `app > res > layout`.

![](https://i.imgur.com/0NBqEqH.png)

### Descriptions

| File                        | Description                                                                                              |
| --------------------------- | -------------------------------------------------------------------------------------------------------- |
| activity_main.xml           | Contains a navigation bar and where the fragment gets displayed.                                         |
| fragment_data.xml           | Contains a list of recordings of the RSSI values and buttons to import and export csv files.             |
| fragment_home.xml           | Contains the interface needed to train the model and predict the amount of vehicles in the parking slot. |
| fragment_scan_router.xml    | Contains a menu to select the routers to be recorded.                                                    |
| fragment_session.xml        | Contains a list of sessions to be loaded.                                                                |
| layout_data_form.xml        |                                                                                                          |
| layout_data_row.xml         |                                                                                                          |
| layout_data_value_input.xml |                                                                                                          |