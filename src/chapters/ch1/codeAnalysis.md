## Code Analysis

### MainActivity.kt
```kt
{{#include ../../scripts/MainActivity.kt:12:35}}
```

{{#include codeSnippetTable.md}}

In order for this code to make sense, we must also see how it looks in the context of the app, so we would also need to understand the `activity_main.xml` and `mobile_navigation.xml` files.

### Understanding the layout

#### activity_main
{{#include screens.md:11:15}}

Once we open up `activity_main.xml` we can see the app, and more importantly, its layout. If we look at the layout, we can see that at the bottom portion of our screen, we have a bar that is named `nav_view` which hosts the different buttons we need to navigate our app and the rest of the screen is taken up by the `nav_host_fragment_activity_main`, which would display the different screens of the app.

In our `MainActivity.kt` file, we assign these properties to variables that we can access in code. The `nav_view` property is set to the `navView` variable, and the `nav_host_fragment_activity_main` is set to the `navController` variable.

```kt
{{#include ../../scripts/MainActivity.kt:22:24}}
```

To control the fragments shown on the screen, we need to create an `appBarConfiguration` which determines the elements for the `navView`, and for this we would need the `mobile_navigation.xml`.

#### mobile_navigation
{{#include screens.md:3:8}}

If we open up `mobile_navigation.xml`, we can see the different screens of our app, and when we look at the code side of things we can see that the ids of each fragment correspond to the parameters which have been set in the `appBarConfiguration`.
```xml
{{#include ../../scripts/navigation/mobile_navigation.xml:8:12}}
```
```xml
{{#include ../../scripts/navigation/mobile_navigation.xml:14:18}}
```
```xml
{{#include ../../scripts/navigation/mobile_navigation.xml:20:24}}
```
```kt
{{#include ../../scripts/MainActivity.kt:25:30}}
```

Once the `appBarConfiguration` has been made, we would now apply it to the `navController` so that when we select an option from the `nav_view` portion of our screen, it would reflect on the `nav_host_fragment_activity_main` portion as well.

```kt
{{#include ../../scripts/MainActivity.kt:32:33}}
```