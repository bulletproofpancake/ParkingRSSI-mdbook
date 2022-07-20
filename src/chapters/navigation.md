# App Navigation

The UI of the app uses [Fragments](https://developer.android.com/guide/fragments) in order to quickly be able to load different screens at any point in time. This is done in order for the app to only have one [Activity](https://developer.android.com/guide/components/activities/intro-activities) and have all of the needed components loaded as fragments.

> **NOTE:** Learn more about the differences between Activities and Fragments [here](https://blog.avenuecode.com/android-basics-activities-fragments#:~:text=Activity%20is%20the%20part%20where,user%20interface%20in%20an%20Activity.).

## Writer's notes
From what I understand, the usage of fragments instead of activities for app navigation is similar to Unity's `OnEnable` and `Instantiate` systems respectively.

### Instantiation
In Unity, instantiating objects often is very heavy for the program because it would create new copies of a component and destroy that copy afterwards, increasing the amount of tasks the program would do. Destroying the copies also means that whenever we would need that screen again, all of the existing properties that have been changed during the program's life would be defaulted unless these properties are saved somewhere.

![Activity Instantiation Unity](https://i.imgur.com/PFmayFV.gif)

In the Hierarchy window on the upper-right side of the window, when a screen gets selected, a clone of the original copy gets "instantiated" or appears into the screen, hence the *(Clone)* suffix.

![](https://i.imgur.com/98u3ugY.png)

When other screens get selected, the previous screen gets removed and another screen takes its place.

![](https://i.imgur.com/RURU708.gif)

Because these copies are destroyed when they are no longer used, any changes made to them will disappear as well when we load a screen, even if it is the same screen.

![](https://i.imgur.com/dSM0Za7.gif)

As far as I can tell, an `Activity` in Android Studio is similar to the screen that have shown. Every time another screen or activity gets loaded, all of the changes made to that activity will be removed.