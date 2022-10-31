# Views

The views are the actual elements that we interact with in the app.
This means that views are where the classes are instantiated, methods are called, etc.

The project has two main "views", `Activities` and `Fragments`.
Activities are the user interface components of Android Studio and
usually used to create a single screen.
Fragments are used in order to be able to display multiple "screens"
on a single activity.

Looking at the `activity_main.xml`, there are two major components;
the `nav_view` and the `nav_host_fragment_activity_main` which is where
the fragments of the screens are located.

![](https://i.imgur.com/Mdu9dlC.png)

The `nav_view` is where the buttons to load the different fragments to display on the `nav_host_fragment_activity_main`.

## onCreateView and onDestroyView

The fragments override the `onCreateView` and `onDestroyView` methods.
This only means that code is executed whenever that fragment has been loaded or unloaded.

Let's take a look at [`SessionFragment`](sessionFragment.md) for example.

```kt
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    // Connects the script to the elements in the xml file
    _binding = FragmentSessionBinding.inflate(inflater, container, false)
    val root: View = binding.root

    // Gets a reference to the prefs singleton
    prefs = Prefs.getInstance(requireContext())!!
    // Gets a reference to the dbHelper singleton
    db = DBHelper.getInstance(requireContext())!!

    // Sets up the session view
    setup()

    // Returns the root which is needed by the function
    return root
}
```

As we can see, on the `onCreateView`, everything the application will need gets loaded and setup.

```kt
override fun onDestroyView() {
    super.onDestroyView()
    // Clears the connection of the script to the elements
    _binding = null
}
```

While on the `onDestroyView`, the connections of the script and elements gets unloaded.