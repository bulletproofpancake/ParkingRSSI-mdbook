# Scan Router Fragment

## Layout

![](https://i.imgur.com/AgiU6tC.jpg)

## Snippet

```kt
class ScanRouterFragment : Fragment() {
  private val TAG = "SCAN"

  private var _binding: FragmentScanRouterBinding? = null
  private val binding get() = _binding!!

  private lateinit var db: DBHelper
  private lateinit var prefs: Prefs

  private var wifiScanReceiver: BroadcastReceiver? = null
  private lateinit var wifiManager: WifiManager
  private val routers = arrayListOf<ArrayList<Router?>>()
  private lateinit var session: Session
  private var pos: Pair<Int, Int> = Pair(-1, -1)
  private var cellSelected: TextView? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentScanRouterBinding.inflate(inflater, container, false)
    val root: View = binding.root

    // Setup up prefs and default values
    db = DBHelper.getInstance(requireContext())!!
    prefs = Prefs.getInstance(requireContext())!!
    
    val lSession = db.getSessionById(prefs.getSession())

    if (lSession == null) {
      findNavController().navigate(R.id.action_navigation_scan_routers_to_navigation_session)
      return root
    }

    session = lSession
    binding.etRow.setText(session.rows.toString())
    binding.etCol.setText(session.cols.toString())
    setup()

    return root
  }

  private fun setup() {
    binding.llRouters.removeAllViews()

    reset()
    setupWifiManager()
    setupRouters()
    setupGrid()
    setupListeners()
  }

  private fun reset() {
    routers.clear()
    for (row in 0 until session.rows) {
      routers.add(ArrayList())
      for (col in 0 until session.cols) {
        routers[row].add(null)
      }
    }

    binding.glRouterMatrix.removeAllViews()

    binding.glRouterMatrix.rowCount = session.rows
    binding.glRouterMatrix.columnCount = session.cols
  }

  private fun setupWifiManager() {
    wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (!wifiManager.isWifiEnabled) {
      Toast.makeText(context, "Enable your wifi", Toast.LENGTH_LONG).show()
      // TODO: Change navigation
    }

    wifiScanReceiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent) {
        val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        if (success) scanSuccess() else scanFailure()
      }
    }

    val intentFilter = IntentFilter()
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    requireContext().registerReceiver(wifiScanReceiver, intentFilter)
  }

  private  fun setupRouters() {
    Log.d(TAG, "Setup Routers")

    val session = prefs.getSession()
    if (session == 0L) { return }
    val dbRouters = db.getSessionRouters(session)
    for (router in dbRouters) {
      routers[router.row][router.col] = router
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupGrid() {
    Log.d(TAG, "Setup Grid")

    // Setup the Grid Buttons
    for (row in 0 until session.rows) {
      for (col in 0 until session.cols) {
        // Layout and Creation
        val tvCell = TextView(context)
        val params = GridLayout.LayoutParams(
          GridLayout.spec(GridLayout.UNDEFINED, 1, 1f),
          GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
        )

        tvCell.height = 128
        tvCell.width = ViewGroup.LayoutParams.WRAP_CONTENT

        tvCell.setBackgroundColor(if ((row + col) % 2 == 0) Color.RED else Color.BLUE)
        tvCell.setOnClickListener {
          if (cellSelected != null) {
            val (r, c) = pos
            cellSelected!!.setBackgroundColor(if ((r + c) % 2 == 0) Color.RED else Color.BLUE)
          }

          pos = Pair(row, col)
          tvCell.setBackgroundColor(Color.WHITE)
          tvCell.setTextColor(Color.BLACK)
          cellSelected = tvCell
        }

        binding.glRouterMatrix.addView(tvCell, params)

        // Text Formatting
        tvCell.foregroundGravity = Gravity.CENTER
        tvCell.gravity = Gravity.CENTER
        tvCell.textSize = 16f
        tvCell.typeface = Typeface.DEFAULT_BOLD
        tvCell.setTextColor(Color.WHITE)

        val router = routers[row][col]
        val text = if (router != null) "${router.getName()}\n${router.getBSSIDStr()}" else "NULL"
        tvCell.text = text
      }
    }

    binding.glRouterMatrix.invalidate()
  }

  private fun setupListeners() {
    binding.btnSetMatrix.setOnClickListener {
      val row = binding.etRow.text.toString().toInt()
      val col = binding.etCol.text.toString().toInt()
      Log.d(TAG, "Pressed ($col, $row)")

      if (session.rows != row || session.cols != col) {
        session.rows = row
        session.cols = col

        // TODO: Transactional
        db.deleteSessionRows(session.id)
        db.deleteSessionRouters(session.id)
        db.updateSession(session)

        reset()
        setupGrid()
      }
    }

    binding.btnScanRouters.setOnClickListener {
      Log.d(TAG, "Start Scanning")

      val scanning = wifiManager.startScan()
      if (scanning) {
        binding.llRouters.removeAllViews()
        binding.progressbar.visibility = View.VISIBLE
      } else {
        Toast.makeText(requireContext(),
          "Could not start scan, Location might be off",
          Toast.LENGTH_LONG).show()
      }
    }
  }

  private fun scanSuccess() {
    Log.d(TAG, "Scan Successful")

    binding.progressbar.visibility = View.INVISIBLE
    val llRouters = binding.llRouters

    val results = wifiManager.scanResults
    for (result in results) {
      val btn = Button(context)

      val text = "${result.SSID}<${result.BSSID}>"
      btn.text = text

      btn.setOnClickListener {
        if (cellSelected == null) {
          return@setOnClickListener
        }

        val (SSID, BSSID) = btn.text.subSequence(0, btn.text.length - 1).split("<")
        val (r, c) = pos
        val router = Router(BSSID, SSID)
        router.row = r
        router.col = c

        val btnTxt = "${router.getName()}\n${router.getBSSIDStr()}"

        pos = Pair(-1, -1)

        routers[r][c] = router

        cellSelected!!.setBackgroundColor(if ((r + c) % 2 == 0) Color.RED else Color.BLUE)
        cellSelected!!.text = btnTxt

        cellSelected = null

        btn.isEnabled = false

        try {
          db.addRouter(router.getBSSID(), router.getName())
          db.addSessionRouter(prefs.getSession(), router.getBSSID(), r, c)
        } catch (e: Exception) {
          Log.d(TAG,"Could not add router")
        }
      }

      llRouters.addView(btn)
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