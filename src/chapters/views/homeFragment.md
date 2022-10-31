# Home Fragment

The home fragment is where the data can be recorded and predicted based on the routers set up on the [Scan Router Fragment](scanRouterFragment.md).

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

  // Determines the state of the application
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

    // Gets the instance of the prefs and dbhelper
    prefs = Prefs.getInstance(requireContext())!!
    db = DBHelper.getInstance(requireContext())!!
    // Gets the session from the database
    val lSession = db.getSessionById(prefs.getSession())
    // Gets the capacity from the preferences
    capacity = prefs.getCapacity()

    // Sets the text in the capacity field to the current capacity
    binding.etCapacity.setText(capacity.toString())

    _context = requireContext()

    // Checks if there is a session saved and opens the session fragment if there is none
    if (lSession == null) {
      findNavController().navigate(R.id.action_navigation_home_to_navigation_session)
      return root
    }

    // Sets the global session to the current session
    session = lSession
    // Sets up the other components
    setup()

    return root
  }

  private fun setup() {
    setupWifiManager()
    setupListeners()
    setupGrid()
    setupKNN()
  }

  // Similar to how the wifi manager is setup in Scan Router Fragment
  private fun setupWifiManager() {
    wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (!wifiManager.isWifiEnabled) {
      Toast.makeText(context, "Enable your wifi", Toast.LENGTH_LONG).show()
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
    // Saves the new capacity to the preferences
    // when it has been changed in the input field
    binding.etCapacity.doOnTextChanged { text, _, _, _ ->
      val t = text.toString()
      if (t != "") {
        capacity = t.toInt()
        prefs.setCapacity(capacity)
      }
    }

    // Saves the new label to the preferences
    // when it has been changed in the input field
    binding.etLabel.doOnTextChanged { text, _, _, _ ->
      val t = text.toString()
      if (t != "") {
        val i = t.toInt()
        label = i
      }
    }

    // Button Clicks
    // Sets what happens when the Record button is clicked
    binding.btnRecord.setOnClickListener {
      setupScan()

      state = State.RECORDING
    }

    // Sets what happens when the Predict button is clicked
    binding.btnScanning.setOnClickListener {
      setupScan()

      state = State.PREDICTING
    }
  }

  // Sets up the router matrix visual similar to the Scan Router Fragment
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
    // Gets the current session saved in the device
    val session = prefs.getSession()
    if (session == 0L) {
      // Does not continue if there are no sessions found
      return
    }

    // Loads the data of the session from the database to the KNN Classifier class
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
    // Executes the code within after a certain amount of time
    handler.postDelayed({
      // Clears the existing list of routers
      routers.clear()

      // Gets the current session from the device
      val session = prefs.getSession()
      if (session > 0L) {
        // Gets routers from the database for the session
        val dbRouters = db.getSessionRouters(session)
        for (router in dbRouters) {
          // Sets the routers in the view with the routers from the database
          routers[router.getBSSID()] = router
        }
        // Sets the input size of the knn classifier to the number of routers
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

    // Gets the results of the wifi scan
    val results = wifiManager.scanResults

    when (state) {
      // Sets up what happens when the state is set to predicting
      State.PREDICTING -> {
        // Creates a float array to store the RSSI values
        val vector = FloatArray(routers.size)
        for (result in results) {
          // Grabs a router from the results of the wifiManager
          var router = Router(result.BSSID, result.SSID)
          if (routers.contains(router.getBSSID()) && routers[router.getBSSID()] != null) {
            // Gets the RSSI value of the router and adds it to the vector
            router = routers[router.getBSSID()]!!
            val dbm = result.level
            vector[router.row * session.cols + router.col] = dbm.toFloat()

            // Displays the RSSI value in the router matrix in the home fragment view
            outputMatrix[router.row][router.col].text = dbm.toString()
          }
        }

        // Runs the predict method of the knnClassifier and collects the
        // amount of occupied vehicles detected for the set of RSSI values stored in vector
        val prediction = knnClassifier.predict(vector.toCollection(ArrayList()))
        Log.i(TAG, "Capacity: $capacity ; Prediction: $prediction")
        // Displays the amount of occupied vehicles in the home view
        binding.tvOccupied.text = "Occupied: $prediction"
        // Displays the amount of unoccupied vehicles in the home view
        binding.tvUnoccupied.text = "Unoccupied: ${capacity - prediction}"

        state = State.NONE
      }
      // Sets up what happens when the state is set to recording
      State.RECORDING -> {
        // Creates a float array to store the RSSI values
        val vector = FloatArray(routers.size)
        // Creates a hashmap to store the router id and its associated RSSI value
        val values = hashMapOf<Long, Float>()
        // Initializes the hashmap
        for ((key, _) in routers) {
          values[key] = 0f
        }

        for (result in results) {
          var router = Router(result.BSSID, result.SSID)
          if (routers.contains(router.getBSSID()) && routers[router.getBSSID()] != null) {
            // Grabs a router from the results of the wifiManager
            router = routers[router.getBSSID()]!!
            // Gets the RSSI value of the router and adds it to the vector
            val dbm = result.level.toFloat()
            vector[router.row * session.cols + router.col] = dbm

            // Displays the RSSI value in the router matrix in the home fragment view
            outputMatrix[router.row][router.col].text = "$dbm dbm"
            // Pairs the RSSI value with the router
            values[router.getBSSID()] = dbm
          }
        }

        Log.d(TAG, "HELLO")
        Log.d(TAG, values.toString())

        Log.d(TAG, "Vector: ${vector.joinToString(", ", "[ ", " ]")} ; Label: $label")

        // Adds the point to the knn classifier with the paired label
        knnClassifier.addPoint(
          vector.toCollection(ArrayList()),
          label
        )

        // Adds the points as a row in the database
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