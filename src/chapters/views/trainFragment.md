# Train Fragment

The train fragment is responsible for recording and predicting data points
according to the routers provided in the [Router Fragment](scanRouterFragment.md)

## Snippet

```kt
class TrainFragment : Fragment() {

  private var _binding: FragmentTrainBinding? = null

  private val TAG = "TrainFragment"

  private lateinit var db: DBHelper
  private lateinit var prefs: Prefs
  private var wifiReceiver: BroadcastReceiver? = null
  private lateinit var wifiManager: WifiManager

  // HashMap <BSSID, Router>
  private val routers = hashMapOf<Long, Router>()
  private val outputs = hashMapOf<Long, View>()
  private val knnClassifier: KNNClassifier = KNNClassifier()
  private val matrix = arrayListOf<IntArray>()

  var capacity: Int = 0
  var label: Int = 10
  private lateinit var session: Session
  private var scanTick: Int = 0
  private val maxIteration: Int = 1 // DEBUG

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
    _binding = FragmentTrainBinding.inflate(inflater, container, false)
    val root: View = binding.root

    // Gets the instance of the prefs and dbhelper
    prefs = Prefs.getInstance(requireContext())!!
    db = DBHelper.getInstance(requireContext())!!
    // Gets the session from the database 
    val lSession = db.getSessionById(prefs.getSession())
    // Gets the session stored on the device
    capacity = prefs.getCapacity()

    // Sets the text in the capacity field to the current capacity
    binding.etCapacity.setText(capacity.toString())

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
    setupGrid()
    setupListeners()
    setupKNN()
  }

  // Similar to how the wifi manager is set up in the Router Fragment
  private fun setupWifiManager() {
    wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (!wifiManager.isWifiEnabled) {
      Toast.makeText(context, "Enable your wifi", Toast.LENGTH_LONG).show()
    }

    wifiReceiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent) {
        val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        if (success) scanSuccess() else scanFailure()
      }
    }

    val intentFilter = IntentFilter()
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    requireContext().registerReceiver(wifiReceiver, intentFilter)
  }

  // Sets up the ui for the routers used 
  private fun setupGrid() {
    binding.llRouters.removeAllViews()

    val sessRouters = db.getSessionRouters(session.id)

    for (i in 0 until sessRouters.size) {
      matrix.add(IntArray(maxIteration))
    }

    for (router in sessRouters) {
      val v = LayoutInflater.from(requireContext())
        .inflate(R.layout.layout_router, null)
      val name = v.findViewById<TextView>(R.id.tv_name)
      name.text = router.getName()

      outputs[router.getBSSID()] = v
      binding.llRouters.addView(v)
    }
  }

  private fun setupListeners() {
    // Text Changes
    // Records the capacity to the device whenever it is changed in the text field
    binding.etCapacity.doOnTextChanged { text, _, _, _ ->
      val t = text.toString()
      if (t != "") {
        capacity = t.toInt()
        prefs.setCapacity(capacity)
      }
    }

    // Records the label to the device whenever it is changed in the text field
    binding.etLabel.doOnTextChanged { text, _, _, _ ->
      val t = text.toString()
      if (t != "") {
        val i = t.toInt()
        label = i
      }
    }

    // Button Clicks
    // Makes the record button set up a scan and changes the state of the app
    binding.btnRecord.setOnClickListener {
      setupScan()

      state = State.RECORDING
    }

    // Makes the predict button set up a scan and changes the state of the app
    binding.btnScanning.setOnClickListener {
      // Ensures that there has been at least 3 recorded data for the algorithm
      // before continuing
      if (knnClassifier.size() < 3) {
        Toast.makeText(requireContext(),
          "There should be at least 3 datapoints",
          Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      setupScan()

      state = State.PREDICTING
    }
  }

  private fun setupKNN() {
    // Gets the current session saved in the device
    val session = prefs.getSession()
    if (session == 0L) {
      // Does not continue if there is no current session saved
      return
    }

    // Loads the matrix from the database according to the session
    knnClassifier.loadMatrix(db.getData(session))
  }

  // Allows the buttons in the UI to be used
  private fun enableButtons() {
    binding.btnRecord.isEnabled = true
    binding.btnScanning.isEnabled = true
  }

  // Resets the ui for the grid
  private fun resetGridText() {
    for (v in outputs.values) {
      v.findViewById<ImageView>(R.id.iv_icon)
        .setBackgroundResource(R.drawable.yellow_circle)
    }
  }

  // Prevents the buttons to be used
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
    scanTick = 0

    resetGridText()
    disableButtons()

    val handler = Handler()
    // Executes the code within the braces after a certain amount of time
    handler.postDelayed({
      // Clears the existing list of routers
      routers.clear()

      // Gets the current session from the device
      val session = prefs.getSession()
      if (session > 0L) {
        // Gets the routers from the database based on the session
        val dbRouters = db.getSessionRouters(session)
        for (router in dbRouters) {
          // Sets the routers in the view with the routers from the database
          routers[router.getBSSID()] = router
        }
        // Sets the input size of the knn classifier to the number of routers
        knnClassifier.inputSize = routers.size

        // Does not allow the usage of the buttons if the wifi manager is scanning
        if (!wifiManager.startScan()) {
          enableButtons()
        }
      }
    }, 2500) // 2.5 sec delay
  }

  @SuppressLint("SetTextI18n")
  private fun scanSuccess() {
    // Gets the results of the wifi scan
    val results = wifiManager.scanResults

    // Get the current vector
    for (result in results) {
      // Creates an instance of a router based on the results
      var router = Router(result.BSSID, result.SSID)
      if (routers.contains(router.getBSSID()) && routers[router.getBSSID()] != null) {
        Log.d(TAG, "${router.getBSSIDStr()} ${result.level}")
        // Gets the RSSI value of the router and adds it to the matrix
        router = routers[router.getBSSID()]!!
        matrix[router.row * session.cols + router.col][scanTick] = result.level
      }
    }

    // Get the 4 ticks here
    if (++scanTick < maxIteration) {
      if (!wifiManager.startScan()) {
        enableButtons()
      }
      return
    }

    // Aggregate
    // Collects the RSSI values from the matrix
    val vector = FloatArray(routers.size)
    for ((i, array) in matrix.withIndex()) {
      val filtered = array.filter {
        return@filter it != 0
      }


      vector[i] = if (filtered.isNotEmpty()) filtered.average().toFloat()
        else 0f
    }

    when (state) {
      // Sets up what happens when the state is set to predicting
      State.PREDICTING -> {
        for ((_, router) in routers) {
          // Retrieves the RSSI value from the vector
          val v = vector[router.row * session.cols + router.col]
          // Sets the UI to the RSSI value if available
          val view = outputs[router.getBSSID()] ?: continue
          view.findViewById<TextView>(R.id.tv_value)?.text = "$v dbm"
          view.findViewById<ImageView>(R.id.iv_icon).setBackgroundResource(
            if (v != 0f) R.drawable.green_circle else R.drawable.red_circle
          )
        }

        // Runs the predict method of the knnClassifier and collects the
        // amount of occupied vehicles detected for the set of RSSI values stored in the vector
        val prediction = knnClassifier.predict(vector.toCollection(ArrayList()))
        Log.i(TAG, "Capacity: $capacity ; Prediction: $prediction")
        // Displays the occupied and unoccupied spaces in the UI
        binding.tvOccupied.text = "$prediction"
        binding.tvUnoccupied.text = "${capacity - prediction}"

        // Saves the occupied amount so that in can be used in the home fragment
        prefs.setOccupied(prediction)

        state = State.NONE
      }
      // Sets up what happens when the state is set to recording
      State.RECORDING -> {
        // Creates a hashmap to store the router id and its associated RSSI value
        val values = hashMapOf<Long, Float>()
        // Initializes the hashmap
        for ((key, router) in routers) {
          // Gets the RSSI value for the router from the vector
          val v = vector[router.row * session.cols + router.col]
          // Ties the RSSI value to the associated router id
          values[key] = v
          // Sets the UI to the RSSI value if available
          val view = outputs[router.getBSSID()] ?: continue
          view.findViewById<TextView>(R.id.tv_value)?.text = "$v dbm"
          view.findViewById<ImageView>(R.id.iv_icon).setBackgroundResource(
            if (v != 0f) R.drawable.green_circle else R.drawable.red_circle
          )
        }

        Log.d(TAG, values.toString())
        Log.d(TAG, "Vector: ${vector.joinToString(", ", "[ ", " ]")} ; Label: $label")

        // Runs the addPoint method of the knn classifier
        // with the vector being the point and the label being the amount of cars occupied
        knnClassifier.addPoint(
          vector.toCollection(ArrayList()),
          label
        )

        // Adds the point to the database
        db.addRow(session.id, label, values)
        state = State.NONE
      }
      else -> { /* Do nothing */ }
    }

    enableButtons()
    binding.tvMessage.text = "Scan Successful"
    binding.tvMessage.setBackgroundColor(Color.GREEN)
    Log.d(TAG, "Scan successful")
  }

  @SuppressLint("SetTextI18n")
  private fun scanFailure() {
    for (view in outputs.values) {
      view.findViewById<TextView>(R.id.tv_value)?.text = "0 dbm"
      view.findViewById<ImageView>(R.id.iv_icon)
        .setBackgroundResource(R.drawable.red_circle)
    }

    enableButtons()
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