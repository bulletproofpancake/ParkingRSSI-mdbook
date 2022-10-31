# Session Fragment

The session fragment is the screen where sessions can be added, selected, and deleted.
Sessions are used by the application to keep track of recordings and configurations with the database.

## Layout

![](https://i.imgur.com/4ZBU6jD.jpg)

## Snippet

```kt
class SessionFragment : Fragment() {
	private val TAG = "SESSION"

	private var _binding: FragmentSessionBinding? = null
	private val binding get() = _binding!!

	private lateinit var db: DBHelper
	private lateinit var prefs: Prefs

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

	private fun setup() {
		// Debugging Purpose
		db.printSessions()

		// Clears existing sessions in view
		binding.llSessions.removeAllViews()

		// Gets the session saved in the preferences
		val s = prefs.getSession()
		// Checks for each session from the database
		for (session in db.getAllSessions()) {
			// Adds a button for that session
			addButton(session)

			// Sets the label in the session view to the current session selected
			if (session.id == s) {
				binding.tvSession.text = session.name
			}
		}

		// Sets what happens when the add session button is pressed
		binding.btnAddSession.setOnClickListener {
			// Stores the text written in the session input field
			val text = binding.etSession.text.toString()
			// Checks if the text is empty
			// And does not continue
			if (text.isEmpty()) {
				return@setOnClickListener
			}

			// Adds a session to the database
			val id = db.addSession(text, 2, 3)
			// Clears the text in the input field
			binding.etSession.text.clear()
			// Creates an instance of a session
			val session = Session(id, text, 2, 3)
			// Creates the button for the session
			addButton(session)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun addButton(session: Session) {
		// Creates a new button instance
		val btn = Button(requireContext())
		// Sets the name to the session
		btn.text = session.name
		// Sets what happens when the button is clicked
		btn.setOnClickListener {
			// Saves the session to the preferences
			prefs.setSession(session.id)
			// Sets the label in the session view to the session selected.
			binding.tvSession.text = session.name
		}

  		// Sets what happens when the button is held
		btn.setOnLongClickListener {
			// Delets the session from the database
			db.deleteSession(session.id)
			// Removes the session button from the view
			binding.llSessions.removeView(it)
			if (session.id == prefs.getSession()) {
				// Sets the session saved in the preferences
				// to the default
				prefs.setSession(0L)
			}

			// Sets the label in the session view to the text
			binding.tvSession.text = "No Session Selected"
			return@setOnLongClickListener true
		}

		// Adds the new button to the view
		binding.llSessions.addView(btn)
	}

	override fun onDestroyView() {
		super.onDestroyView()
    	// Clears the connection of the script to the elements
		_binding = null
	}
}

```