## App Navigation

The UI of the app uses [Fragments](https://developer.android.com/guide/fragments) in order to quickly be able to load different screens at any point in time. This is done in order for the app to only have one [Activity](https://developer.android.com/guide/components/activities/intro-activities) and have all of the needed components loaded as fragments.

> **NOTE:** Learn more about the differences between Activities and Fragments [here](https://blog.avenuecode.com/android-basics-activities-fragments#:~:text=Activity%20is%20the%20part%20where,user%20interface%20in%20an%20Activity.).

### Writer's notes

> **TL;DR** `Activities` gets reloaded every time a screen is changed, so in order to prevent data loss, `Fragments` are used.

From what I understand, the usage of fragments instead of activities for app navigation is similar to Unity's `SetActive` and `Instantiate` systems respectively.

#### Instantiation
In Unity, instantiating objects often is very heavy for the program because it would create new copies of a component and destroy that copy afterwards, increasing the amount of tasks the program would do. Destroying the copies also means that whenever we would need that screen again, all of the existing properties that have been changed during the program's life would be defaulted unless these properties are saved somewhere.

![Activity Instantiation Unity](https://i.imgur.com/PFmayFV.gif)

In the Hierarchy window on the upper-right side of the window, when a screen gets selected, a clone of the original copy gets "instantiated" or appears into the screen, hence the *(Clone)* suffix.

![](https://i.imgur.com/98u3ugY.png)

When other screens get selected, the previous screen gets removed and another screen takes its place.

![](https://i.imgur.com/RURU708.gif)

Because these copies are destroyed when they are no longer used, any changes made to them will disappear as well when we load a screen, even if it is the same screen.

![](https://i.imgur.com/dSM0Za7.gif)

As far as I can tell, an `Activity` in Android Studio is similar to the screen that have shown. Every time another screen or activity gets loaded, all of the changes made to that activity will be removed.

#### SetActive
In order to prevent the loss of data upon loading another screen, the developer of the app used `Fragments` which he explained as something like putting the UI on top of the Activity itself, therefore the activity never gets reloaded and never loses its data. In Unity, this can be achieved by the `GameObject.SetActive()` method. When setting a gameObject's active state, we basically suspend the gameObject, allowing us to keep the data it stored before being disabled.

![Activity SetActive Unity](https://i.imgur.com/sPCX3Kk.gif)

As you can see, the screens are still clones but that is because we need to Instantiate them first in order to use them in Unity. This time however, each screen gets lit when they are activated.

![](https://i.imgur.com/sezZMf0.gif)

Because these screens do not get removed, they get to keep their data even when they are unused.

![](https://i.imgur.com/qJPbLt9.gif)

By using `Fragments` instead of an `Activity` to load the necessary scenes in the app, this allows each state of the screens to persist even when they are unloaded.