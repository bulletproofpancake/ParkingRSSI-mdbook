# Session Fragment

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
		_binding = FragmentSessionBinding.inflate(inflater, container, false)
		val root: View = binding.root

		prefs = Prefs.getInstance(requireContext())!!
		db = DBHelper.getInstance(requireContext())!!

		setup()

		return root
	}

	private fun setup() {
		// Debugging Purpose
		db.printSessions()

		binding.llSessions.removeAllViews()

		val s = prefs.getSession()
		for (session in db.getAllSessions()) {
			addButton(session)

			if (session.id == s) {
				binding.tvSession.text = session.name
			}
		}

		binding.btnAddSession.setOnClickListener {
			val text = binding.etSession.text.toString()
			if (text.isEmpty()) {
				return@setOnClickListener
			}

			val id = db.addSession(text, 2, 3)
			binding.etSession.text.clear()
			val session = Session(id, text, 2, 3)
			addButton(session)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun addButton(session: Session) {
		val btn = Button(requireContext())
		btn.text = session.name
		btn.setOnClickListener {
			prefs.setSession(session.id)
			binding.tvSession.text = session.name
		}

		btn.setOnLongClickListener {
			db.deleteSession(session.id)
			binding.llSessions.removeView(it)
			if (session.id == prefs.getSession()) {
				prefs.setSession(0L)
			}

			binding.tvSession.text = "No Session Selected"
			return@setOnLongClickListener true
		}

		binding.llSessions.addView(btn)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

```