## Code Analysis

```kt
{{#include ../../scripts/MainActivity.kt:12:35}}
```

| Snippet                                                 | Description                                                                                                                                                                                                                               |
| ------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `class MainActivity : AppCompatActivity()`              | Declares a class named `MainActivity` that inherits from [`AppCompatActivity`](https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity).                                                                         |
| `private lateinit var binding : ActivityMainBinding`    | [Declares a variable named `binding` of which is used to bind the code to the `activity_main.xml` layout.](https://developer.android.com/topic/libraries/data-binding/expressions#binding_data)                                           |
| `override fun onCreate(savedInstanceState:Bundle?)`     | Overrides the `onCreate` function from [`Activity`](https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle)) *(the class which `AppCompatActivity` inherits from)*.                                      |
| `super.onCreate(savedInstanceState)`                    | Executes the `onCreate` function as it is implemented by the classes `MainActivity` inherits from.                                                                                                                                        |
| `binding = ActivityMainBinding.inflate(layoutInflated)` | [Makes the contents of the xml file bound to `ActivityMainBinding` accessible.](https://stackoverflow.com/questions/4576330/what-does-it-mean-to-inflate-a-view-from-an-xml-file)                                                         |
| `setContentView(binding.root)`                          | [Sets the content view](https://developer.android.com/reference/android/app/Activity#setContentView(android.view.View)) on the [`root`](https://developer.android.com/reference/android/databinding/ViewDataBinding#getroot) of the view. |