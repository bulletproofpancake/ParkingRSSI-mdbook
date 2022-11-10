# Router Fragment

The router fragment is the screen where routers are scanned, configured, and saved to the database.

## Layout

![](https://i.imgur.com/AgiU6tC.jpg)

## Snippet

```kt
class RouterFragment : Fragment() {
	private val TAG = "RouterFragment"

  private var _binding: FragmentRouterBinding? = null
  private val binding get() = _binding!!

  private lateinit var db: DBHelper
  private lateinit var prefs: Prefs
  private var wifiScanReceiver: BroadcastReceiver? = null
  private lateinit var wifiManager: WifiManager

	private var unselColor: Int = 0
	private var selColor: Int = 0
	private val map = hashMapOf<String, Boolean>()
	private lateinit var session: Session

  override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
  ): View {
    // Connects the script to the elements in the xml file
		_binding = FragmentRouterBinding.inflate(inflater, container, false)
		val root: View = binding.root

    // Gets the instance of the prefs and dbhelper
    db = DBHelper.getInstance(requireContext())!!
    prefs = Prefs.getInstance(requireContext())!!
    // Gets the current session from the preferences and stores it locally
    val lSession = db.getSessionById(prefs.getSession())

    // Sets the colors for when a router is selected or unselected in the UI
	  unselColor = requireContext().resources.getColor(R.color.gray, null)
		selColor = requireContext().resources.getColor(R.color.purple_200, null)

    if (lSession == null) {
      // Goes to the session screen if there are no sessions found
      findNavController().navigate(R.id.action_navigation_router_to_navigation_session)
      return root
    }

    // Sets the global reference to the local session
	  session = lSession
    // Sets up the rest of the view
	  setup()

    return root
  }

	private fun setup() {
		setupWifiManager()
		setupView()
		setupListeners()
	}

  private fun setupWifiManager() {
    // Gets a reference to the Wifi Manager
    wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (!wifiManager.isWifiEnabled) {
      Toast.makeText(context, "Enable your wifi", Toast.LENGTH_LONG).show()
      // TODO: Change navigation
    }

    // The receiver which detects the changes in the wifi
    wifiScanReceiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent) {
        // Checks if wifi scan is successful
        val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        if (success) scanSuccess() else scanFailure()
      }
    }

    // Filters the amount of Intent that can be received
    val intentFilter = IntentFilter()
    // Adds the WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
    // to the intents that will be received
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    // Registers the wifiScanReceiver with the intentFilter
    requireContext().registerReceiver(wifiScanReceiver, intentFilter)
  }

  // Sets up the UI for the router selection
	private fun setupView() {
		binding.btnSet.setBackgroundResource(R.drawable.rounded_edges)
		binding.btnScan.setBackgroundResource(R.drawable.rounded_edges)

    // Removes existing items in the list view
		binding.llRouters.removeAllViews()

		// Add the routers that are added to the list view
		val routers = db.getSessionRouters(session.id)
		for (router in routers) {
			val btn = Button(context)
			btn.setBackgroundColor(selColor)
			val text = "${router.getName()}<${router.getBSSIDStr()}>"
			btn.text = text

			binding.llRouters.addView(btn)
		}
	}

	private fun setupListeners() {
    // Sets up what happens when the routers button is clicked
		binding.btnScan.setOnClickListener {
			Log.d(TAG, "Scanning Routers")

      // The wifi manager starts scanning the network
      // This sends out the intent WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
      // which creates the buttons for each router to be selected in scanSuccess
      val scanning = wifiManager.startScan()
      if (scanning) {
        // Clears the existing routers in the view
        binding.llRouters.removeAllViews()
      } else {
        Toast.makeText(requireContext(),
          "Could not start scan, Location might be off",
          Toast.LENGTH_LONG).show()
      }
		}

		binding.btnSet.setOnClickListener {
			Log.d(TAG, "Setting Routers")

			db.deleteSessionRouters(session.id)

			var r = 0
			for ((key, value) in map) {
				if (!value) {
					continue
				}

				val (ssid, bssid) = key.subSequence(0, key.length - 1).split("<")
				val router = Router(bssid, ssid)
				db.addRouter(router.getBSSID(), router.getName())
				db.addSessionRouter(session.id, router.getBSSID(), r, 0)
				r++
			}

			session.cols = 1
			session.rows = r
			db.updateSession(session)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun scanSuccess() {
		Log.d(TAG, "Scan Success")

		// Add the routers
		binding.llRouters.removeAllViews()
		map.clear()

    // Gets the results of the scan from the wifiManager
		val results = wifiManager.scanResults

		for (result in results) {
      // Creates a button for each router
			val btn = Button(context)
			btn.setBackgroundColor(unselColor)
      // Sets the name of the button according to
      // the name and address of the router
			val text = "${result.SSID}<${result.BSSID}>"
			btn.text = text
			map[result.BSSID] = false

      // Sets up what happens when the entry for the router is clicked
			btn.setOnClickListener {
				val hi = map[result.BSSID]?: false
				if (hi) {
					map[text] = false
					btn.setBackgroundColor(unselColor)
				} else {
					map[text] = true
					btn.setBackgroundColor(selColor)
				}
			}

      // Adds the button to the lsit view
			binding.llRouters.addView(btn)
		}
	}

	private fun scanFailure() {
		Log.e(TAG, "Scan Failed")
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null

    if (wifiScanReceiver != null) {
      requireContext().unregisterReceiver(wifiScanReceiver)
      wifiScanReceiver = null
    }
	}
}

```
