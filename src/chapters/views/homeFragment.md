# Home Fragment

The home fragment is where the results of the algorithm are displayed.

## Layout

![](https://i.imgur.com/kJ5omXX.jpg)

## Snippet

```kt
class HomeFragment : Fragment() {

  private var _binding: FragmentHomeBinding? = null

  private val TAG = "HOME"

  private lateinit var db: DBHelper
  private lateinit var prefs: Prefs
  private var wifiReceiver: BroadcastReceiver? = null
  private lateinit var wifiManager: WifiManager
  private lateinit var session: Session

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    val root: View = binding.root

    // Gets the instance of the prefs and dbHelper
    prefs = Prefs.getInstance(requireContext())!!
    db = DBHelper.getInstance(requireContext())!!
    // Gets the last session from the database
    val lSession = db.getSessionById(prefs.getSession())

    // Gets the capacity and occupied count saved to the device
    val capacity = prefs.getCapacity()
    val occupied = prefs.getOccupied()
    // Sets the capacity, occupied, and unoccupied numbers to the UI
    binding.tvCapacity.text = capacity.toString()
    binding.tvOccupied.text = occupied.toString()
    binding.tvUnoccupied.text = (capacity - occupied).toString()

    // If there are no previous sessions
    // Stay on the same screen
    if (lSession == null) {
      return root
    }

    session = lSession
    return root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null

    if (wifiReceiver != null) {
      requireContext().unregisterReceiver(wifiReceiver)
      wifiReceiver = null
    }
  }
}

```