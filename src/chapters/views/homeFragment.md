# Home Fragment



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

  // HashMap <BSSID, Router>
  private val routers = hashMapOf<Long, Router>()
  private val knnClassifier: KNNClassifier = KNNClassifier()
  private val outputMatrix = arrayListOf<ArrayList<TextView>>()
  private lateinit  var _context: Context

  var capacity: Int = 0
  var label: Int = 0
  private lateinit var session: Session

  private enum class State {
    RECORDING,
    PREDICTING,
    NONE
  }
  private var state = State.NONE

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

    prefs = Prefs.getInstance(requireContext())!!
    db = DBHelper.getInstance(requireContext())!!
    val lSession = db.getSessionById(prefs.getSession())
    capacity = prefs.getCapacity()

    binding.etCapacity.setText(capacity.toString())

    _context = requireContext()

    if (lSession == null) {
      findNavController().navigate(R.id.action_navigation_home_to_navigation_session)
      return root
    }

    session = lSession
    setup()

    return root
  }

  private fun setup() {
    setupWifiManager()
    setupListeners()
    setupGrid()
    setupKNN()
  }

  private fun setupWifiManager() {
    wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (!wifiManager.isWifiEnabled) {
      Toast.makeText(context, "Enable your wifi", Toast.LENGTH_LONG).show()
      // TODO: Graceful exit
    }

    wifiReceiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent) {
        enableButtons()
        val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        if (success) scanSuccess() else scanFailure()
      }
    }

    val intentFilter = IntentFilter()
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    requireContext().registerReceiver(wifiReceiver, intentFilter)
  }

  private fun setupListeners() {
    // Text Changes
    binding.etCapacity.doOnTextChanged { text, _, _, _ ->
      val t = text.toString()
      if (t != "") {
        capacity = t.toInt()
        prefs.setCapacity(capacity)
      }
    }

    binding.etLabel.doOnTextChanged { text, _, _, _ ->
      val t = text.toString()
      if (t != "") {
        val i = t.toInt()
        label = i
      }
    }

    // Button Clicks
    binding.btnRecord.setOnClickListener {
      setupScan()

      state = State.RECORDING
    }

    binding.btnScanning.setOnClickListener {
      setupScan()

      state = State.PREDICTING
    }
  }

  private fun setupGrid() {
    // Clear values
    binding.glOutputMatrix.removeAllViews()
    outputMatrix.clear()

    binding.glOutputMatrix.rowCount = session.rows
    binding.glOutputMatrix.columnCount = session.cols

    // setupScan the Grid Buttons
    for (row in 0 until session.rows) {
      outputMatrix.add(arrayListOf())
      for (col in 0 until session.cols) {
        val tv = TextView(context)
        val params = GridLayout.LayoutParams(
          GridLayout.spec(GridLayout.UNDEFINED, 1, 1f),
          GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
        )

        tv.height = 128
        tv.width = ViewGroup.LayoutParams.WRAP_CONTENT
        tv.setBackgroundColor(if ((row + col) % 2 == 0) Color.RED else Color.BLUE)
        tv.text = "0" // TODO: Check if the router is set else set this to not configured
        tv.textSize = 32f
        tv.typeface = Typeface.DEFAULT_BOLD
        tv.gravity = Gravity.CENTER

        binding.glOutputMatrix.addView(tv, params)
        outputMatrix[row].add(tv)
      }
    }
  }

  private fun setupKNN() {
    val session = prefs.getSession()
    if (session == 0L) {
      return
    }

    knnClassifier.loadMatrix(db.getData(session))
  }

  private fun enableButtons() {
    binding.btnRecord.isEnabled = true
    binding.btnScanning.isEnabled = true
  }

  private fun resetGridText() {
    for (row in 0 until session.rows) {
      for (col in 0 until session.cols) {
        outputMatrix[row][col].text = "0"
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun disableButtons() {
    binding.tvMessage.text = ""
    binding.tvMessage.setBackgroundColor(Color.TRANSPARENT)

    binding.btnRecord.isEnabled = false
    binding.btnScanning.isEnabled = false
  }

  /**
   * Setups the routers then initiates the scans
   */
  private fun setupScan() {
    resetGridText()
    disableButtons()

    val handler = Handler()
    handler.postDelayed({
      routers.clear()

      val session = prefs.getSession()
      if (session > 0L) {
        val dbRouters = db.getSessionRouters(session)
        for (router in dbRouters) {
          routers[router.getBSSID()] = router
        }
        knnClassifier.inputSize = routers.size

        if (!wifiManager.startScan()) {
          enableButtons()
        }
      }
    }, 2500) // 2.5 sec delay
  }

  @SuppressLint("SetTextI18n")
  private fun scanSuccess() {
    binding.tvMessage.text = "Scan Successful"
    binding.tvMessage.setBackgroundColor(Color.GREEN)

    val results = wifiManager.scanResults

    when (state) {
      State.PREDICTING -> {
        val vector = FloatArray(routers.size)
        for (result in results) {
          var router = Router(result.BSSID, result.SSID)
          if (routers.contains(router.getBSSID()) && routers[router.getBSSID()] != null) {
            router = routers[router.getBSSID()]!!
            val dbm = result.level
            vector[router.row * session.cols + router.col] = dbm.toFloat()

            outputMatrix[router.row][router.col].text = dbm.toString()
          }
        }

        val prediction = knnClassifier.predict(vector.toCollection(ArrayList()))
        Log.i(TAG, "Capacity: $capacity ; Prediction: $prediction")
        binding.tvOccupied.text = "Occupied: $prediction"
        binding.tvUnoccupied.text = "Unoccupied: ${capacity - prediction}"

        state = State.NONE
      }
      State.RECORDING -> {
        val vector = FloatArray(routers.size)
        val values = hashMapOf<Long, Float>()
        for ((key, _) in routers) {
          values[key] = 0f
        }

        for (result in results) {
          var router = Router(result.BSSID, result.SSID)
          if (routers.contains(router.getBSSID()) && routers[router.getBSSID()] != null) {
            router = routers[router.getBSSID()]!!
            val dbm = result.level.toFloat()
            vector[router.row * session.cols + router.col] = dbm

            outputMatrix[router.row][router.col].text = "$dbm dbm"
            values[router.getBSSID()] = dbm
          }
        }

        Log.d(TAG, "HELLO")
        Log.d(TAG, values.toString())

        Log.d(TAG, "Vector: ${vector.joinToString(", ", "[ ", " ]")} ; Label: $label")

        knnClassifier.addPoint(
          vector.toCollection(ArrayList()),
          label
        )

        db.addRow(prefs.getSession(), label, values)
        state = State.NONE
      }
      else -> { /* Do nothing */ }
    }

    Log.d(TAG, "Scan successful")
  }

  private fun scanFailure() {
    binding.tvMessage.text = "Scan Failed"
    binding.tvMessage.setBackgroundColor(Color.RED)
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