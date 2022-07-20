package com.silentrald.parkingrssi.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.silentrald.parkingrssi.DBHelper
import com.silentrald.parkingrssi.KNNClassifier
import com.silentrald.parkingrssi.Prefs
import com.silentrald.parkingrssi.Router
import com.silentrald.parkingrssi.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val TAG = "HOME"
    
    private val ROW_KEY = "row"
    private val COL_KEY = "col"
    private val CAPACITY_KEY = "capacity"
    
    private lateinit var pref: Prefs
    private lateinit var wifiReceiver: BroadcastReceiver
    private lateinit var wifiManager: WifiManager
    private val routers = HashMap<String, Int>()
    private val knnClassifier: KNNClassifier = KNNClassifier()
    private val outputMatrix = arrayListOf<ArrayList<TextView>>()
    private lateinit  var _context: Context

    var capacity: Int = 0
    var label: Int = 0
    var r: Int = 0
    var c: Int = 0

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
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        pref = Prefs(requireContext())
        r = pref.getInt(ROW_KEY, 3)
        c = pref.getInt(COL_KEY, 2)

        capacity = pref.getInt(CAPACITY_KEY, 10)
        binding.etCapacity.setText(capacity.toString())

        _context = requireContext()

        // setupScan Wifi Scanning
        wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(context, "Enable your wifi", Toast.LENGTH_LONG).show()
            // TODO: Change navigation
        }

        wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) //intent basta dito lang lahat ng data.
                if (success) {
                    scanSuccess(context)
                } else {
                    scanFailure(context)
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        requireContext().registerReceiver(wifiReceiver, intentFilter)

        binding.etCapacity.doOnTextChanged { text, _, _, _ ->
            val t = text.toString()
            if (t != "") {
                val i = t.toInt()
                capacity = i
                pref.setInt(CAPACITY_KEY, i)
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

        setup()

        return root
    }

    private fun setup() {
        knnClassifier.loadMatrix(requireContext())
        setupGrid()
    }

    private fun setupScan() {
        routers.clear()

        val col = pref.getInt(COL_KEY, -1)

        val db = DBHelper(requireContext(), null)
        val cursor = db.getRouters()
        cursor!!.moveToFirst()

        var bssid =
            Router.convertBSSIDToString(cursor.getLong(cursor.getColumnIndex(DBHelper.ROUTER_BSSID)))
        var r = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_ROW))
        var c = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_COL))
        routers[bssid] = r * col + c

        while (cursor.moveToNext()) {
            bssid = Router.convertBSSIDToString(cursor.getLong(cursor.getColumnIndex(DBHelper.ROUTER_BSSID)))
            r = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_ROW))
            c = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_COL))
            routers[bssid] = r * col + c
        }

        knnClassifier.inputSize = routers.size

        wifiManager.startScan() //ito magrerequest ng signals ng wifi
        db.close()
    }

    private fun setupGrid() {
        // Clear values
        binding.glOutputMatrix.removeAllViews()
        outputMatrix.clear()

        binding.glOutputMatrix.rowCount = r
        binding.glOutputMatrix.columnCount = c

        // setupScan the Grid Buttons
        for (row in 0 until r) {
            outputMatrix.add(arrayListOf())
            for (col in 0 until c) {
                val tv = TextView(context)
                val params = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                )

                tv.layoutParams = ViewGroup.LayoutParams(0, 48)
                tv.setBackgroundColor(if ((row + col) % 2 == 0) Color.RED else Color.BLUE)
                tv.text = "0"
                tv.textSize = 32f
                tv.gravity = Gravity.CENTER

                binding.glOutputMatrix.addView(tv, params)
                outputMatrix[row].add(tv)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun scanSuccess(ctx: Context) { //if success yung scan
        val results = wifiManager.scanResults

        when (state) {
            State.PREDICTING -> {
                val vector = FloatArray(routers.size)
                for (result in results) {
                    if (routers.contains(result.BSSID)) {
                        val i = routers[result.BSSID] as Int
                        val dbm = result.level
                        vector[i] = dbm.toFloat()

                        outputMatrix[i / c][i % c].text = "$dbm dbm"

                        Log.i(TAG, "${result.SSID} ; $dbm dbm")
                    }
                }

                val pred = knnClassifier.predict(vector.toCollection(ArrayList()))
                Log.i(TAG, "Capacity: $capacity ; Prediction: $pred")
                binding.tvOccupied.text = "Occupied: $pred"
                binding.tvUnoccupied.text = "Unoccupied: ${capacity - pred}"

                state = State.NONE
            }
            State.RECORDING -> {
                val vector = FloatArray(routers.size)
                for (result in results) {
                    if (routers.contains(result.BSSID)) {
                        val i = routers[result.BSSID] as Int
                        val dbm = result.level
                        vector[i] = dbm.toFloat()

                        outputMatrix[i / c][i % c].text = "$dbm dbm"
                    }
                }

                Log.i(TAG, "Vector: ${vector.joinToString(", ", "[ ", " ]")} ; Label: $label")

                knnClassifier.addPoint(
                    vector.toCollection(ArrayList()),
                    label
                )
                Log.i(TAG, knnClassifier.matrix.joinToString("\n", "[\n", "\n]"))
                Log.i(TAG, knnClassifier.labels.toString())

                knnClassifier.saveMatrix(requireContext())

                state = State.NONE
            }
        }

        Toast.makeText(ctx, "Scan complete", Toast.LENGTH_SHORT).show()
    }

    private fun scanFailure(ctx: Context) {
        Toast.makeText(ctx, "Scan failed", Toast.LENGTH_SHORT).show()
    } //if failed scan

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireContext().unregisterReceiver(wifiReceiver)
    }
}