# Models

Models are classes which are used to store and manage data.
Unlike the [`KNN Classifier`](../scripts/knn-classifier.md) class,
models rarely hold any logic and algorithms within them.
This is because these classes are used to containerize data
so that it can be easily accessed somewhere else.

Take for example the [`Session`](session.md) class:

```kt
class Session(
	val id: Long,
	var name: String,
	var rows: Int,
	var cols: Int
) {
	override fun toString(): String {
		return "Id: $id; Name: $name; Rows: $rows; Cols: $cols\n"
	}
}
```

In the [`Session Fragment`](../views/sessionFragment.md) view,
an instance of the session gets created when a name is given and the `Add Session` button is clicked:

```kt
// Tells the `Add Session` button on the screen
// to execute this code when it is clicked
binding.btnAddSession.setOnClickListener {
    // Gets the name from the text field in the screen
    val text = binding.etSession.text.toString()

    // Does not do anything if there is no name associated
    // with the session
    if (text.isEmpty()) {
        return@setOnClickListener
    }

    // Adds the session to the database
    val id = db.addSession(text, 2, 3)

    // Clears the text field
    binding.etSession.text.clear()

    // Creates an instance of the session model
    val session = Session(id, text, 2, 3)

    // Creates a button in the Sessions screen
    // with the data from the instance
    addButton(session)
}
```

And the model is used when a button for the session is created:

```kt
private fun addButton(session: Session) {
    // A button is created
    val btn = Button(requireContext())
    // The label of the button is retrieved from
    // the name of the session
    btn.text = session.name
    // Tells the created button to
    // execute the code inside when it is clicked
    btn.setOnClickListener {
        prefs.setSession(session.id)
        binding.tvSession.text = session.name
    }

    // Tells the created button to
    // execute the code inside when it is held
    btn.setOnLongClickListener {
        // The records of the session is deleted from the database
        db.deleteSession(session.id)
        // The button is removed from the UI
        binding.llSessions.removeView(it)
        // The session saved in the preferences is reset
        if (session.id == prefs.getSession()) {
            prefs.setSession(0L)
        }
        // The UI is changed in order to indicate
        // the removal of the session
        binding.tvSession.text = "No Session Selected"
        return@setOnLongClickListener true
    }

    binding.llSessions.addView(btn)
}
```

In order to understand models, we only need to focus on the following lines of code:

```kt
// binding.btnAddSession.setOnClickListener {
    val text = binding.etSession.text.toString()
    // if (text.isEmpty()) {
    //     return@setOnClickListener
    // }

    val id = db.addSession(text, 2, 3)
    // binding.etSession.text.clear()
    val session = Session(id, text, 2, 3)
    addButton(session)
}
```

When an instance of a `Session` is created in `val session = Session(id, text, 2, 3)`,
it calls upon the constructor of the `Session` class and sets the appropriate value:

```kt
class Session(
	val id: Long,     // id
	var name: String, // text
	var rows: Int,    // 2
	var cols: Int     // 3
)
```

So the `name` comes from the text set in the input field in the application,
the `id` is retrieved from adding a record of the session to the database with the [`DBHelper`](dbhelper.md) class,
and the `rows` and `cols` are set to `2` and `3` respectively by default.
After creating an instance of the session with this data,
it is then used when a button is added to the screen.

From the instance of the session passed through the `addButton` function,
the `name` is used in order to label the button and the session indicator,
the `id` is used to save the session to the preferences and
delete its entry from the database,
while the `rows` and `cols` are used elsewhere.

```kt
private fun addButton(session: Session) {
    // val btn = Button(requireContext())
    btn.text = session.name
    btn.setOnClickListener {
        prefs.setSession(session.id)
        binding.tvSession.text = session.name
    }
    btn.setOnLongClickListener {
        db.deleteSession(session.id)
        // binding.llSessions.removeView(it)
        if (session.id == prefs.getSession()) {
            prefs.setSession(0L)
        }
        // binding.tvSession.text = "No Session Selected"
        // return@setOnLongClickListener true
    }

    // binding.llSessions.addView(btn)
}
```

In conclusion, by using models,
the program is able to containerize the data it receives from different sources
and use these data from a single source.