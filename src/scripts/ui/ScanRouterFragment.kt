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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.silentrald.parkingrssi.DBHelper
import com.silentrald.parkingrssi.Prefs
import com.silentrald.parkingrssi.Router
import com.silentrald.parkingrssi.databinding.FragmentScanRouterBinding

class ScanRouterFragment : Fragment() { //setting up router
    private val TAG = "SCAN"

    private val ROW_KEY = "row"
    private val COL_KEY = "col"

    private var _binding: FragmentScanRouterBinding? = null
    private val binding get() = _binding!!

    private lateinit var pref: Prefs

    private lateinit var wifiScanReceiver: BroadcastReceiver
    private lateinit var wifiManager: WifiManager
    private val routers: HashMap<String, String> = HashMap()
    private val rtrMatrix: ArrayList<ArrayList<String>> = ArrayList()
    private var dim: Pair<Int, Int> = Pair(3, 2)
    private var pos: Pair<Int, Int> = Pair(-1, -1)
    private var btnSelected: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanRouterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup up pref and default values
        pref = Prefs(requireContext())
        dim = Pair(
            pref.getInt(ROW_KEY, 3),
            pref.getInt(COL_KEY, 2)
        )
        binding.etRow.setText(dim.first.toString())
        binding.etCol.setText(dim.second.toString())

        // Setup Wifi Scanning
        wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(context, "Enable your wifi", Toast.LENGTH_LONG).show()
            // TODO: Change navigation
        }

        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess(ctx)
                } else {
                    scanFailure(ctx)
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        requireContext().registerReceiver(wifiScanReceiver, intentFilter)

        // Setup View
        binding.llRouters.removeAllViews()

        setup()

        // Setup Buttons
        binding.btnSetMatrix.setOnClickListener {
            // TODO: Shared Preference
            val row = binding.etRow.text.toString().toInt()
            val col = binding.etCol.text.toString().toInt()

            if (dim.first != row || dim.second != col) {
                dim = Pair(row, col)

                pref.setInt(ROW_KEY, row)
                pref.setInt(COL_KEY, col)

                val db = DBHelper(requireContext(), null)
                db.removeAllRouters()
                db.setData(arrayListOf(), arrayListOf(), row * col)
                db.close()

                reset()
                setupGrid()
            }
        }

        binding.btnScanRouters.setOnClickListener {
            binding.llRouters.removeAllViews()
            binding.progressbar.visibility = View.VISIBLE

            wifiManager.startScan()
        }

        return root
    }

    private fun setup() {
        reset()
        setupRouters()
        setupGrid()
    }

    private fun reset() {
        rtrMatrix.clear()
        for (row in 0 until dim.first) {
            rtrMatrix.add(ArrayList())
            for (col in 0 until dim.second) {
                rtrMatrix[row].add("")
            }
        }

        binding.glRouterMatrix.removeAllViews()

        binding.glRouterMatrix.rowCount = dim.first
        binding.glRouterMatrix.columnCount = dim.second
    }

    private  fun setupRouters() {
        Log.i(TAG, "Setup Routers")

        val db = DBHelper(requireContext(), null)
        val cursor = db.getRouters()
        if (!cursor!!.moveToFirst()) {
            return
        }

        var bssid =
            Router.convertBSSIDToString(cursor.getLong(cursor.getColumnIndex(DBHelper.ROUTER_BSSID)))
        var ssid =
            cursor.getString(cursor.getColumnIndex(DBHelper.ROUTER_NAME))
        var r = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_ROW))
        var c = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_COL))
        rtrMatrix[r][c] = bssid
        routers[bssid] = ssid

        while (cursor.moveToNext()) {
            bssid = Router.convertBSSIDToString(cursor.getLong(cursor.getColumnIndex(DBHelper.ROUTER_BSSID)))
            ssid = cursor.getString(cursor.getColumnIndex(DBHelper.ROUTER_NAME))
            r = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_ROW))
            c = cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTER_COL))
            rtrMatrix[r][c] = bssid
            routers[bssid] = ssid
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupGrid() {
        Log.i(TAG, "Setup Grid")

        // Setup the Grid Buttons
        for (row in 0 until dim.first) {
            for (col in 0 until dim.second) {
                val btn = Button(context)
                val params = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                )

                btn.layoutParams = ViewGroup.LayoutParams(0, 48)
                btn.setBackgroundColor(if ((row + col) % 2 == 0) Color.RED else Color.BLUE)
                btn.setOnClickListener {
                    if (btnSelected != null) {
                        val (r, c) = pos
                        btnSelected!!.setBackgroundColor(if ((r + c) % 2 == 0) Color.RED else Color.BLUE)
                    }

                    pos = Pair(row, col)
                    btn.setBackgroundColor(Color.WHITE)
                    btnSelected = btn
                }

                binding.glRouterMatrix.addView(btn, params)

                val bssid = rtrMatrix[row][col]
                val text = "${routers[bssid]}\n$bssid"
                btn.text = text
                Log.i(TAG, text)
            }
        }

        binding.glRouterMatrix.invalidate()
    }

    private fun scanSuccess(ctx: Context) {
        Log.i(TAG, "Success")
        binding.progressbar.visibility = View.INVISIBLE
        val llRouters = binding.llRouters

        val db = DBHelper(ctx, null)
        db.removeAllRouters()

        val results = wifiManager.scanResults
        for (result in results) {
            val btn = Button(context)

            val text = "${result.SSID}<${result.BSSID}>"
            btn.text = text

            btn.setOnClickListener {
                if (btnSelected != null) {
                    val (SSID, BSSID) = btn.text.subSequence(0, btn.text.length - 1).split("<")
                    val btnTxt = "$SSID\n$BSSID"

                    val (r, c) = pos
                    pos = Pair(-1, -1)

                    // Set Values
                    if (rtrMatrix[r][c] != "") {
                        // TODO: Re-enable the button
                    }

                    // TODO: Set Preferences
                    rtrMatrix[r][c] = BSSID
                    routers[BSSID] = SSID

                    // Edit Button
                    btnSelected!!.setBackgroundColor(if ((r + c) % 2 == 0) Color.RED else Color.BLUE)
                    btnSelected!!.text = btnTxt

                    btnSelected = null

                    // Disable Button
                    btn.isEnabled = false

                    // Try to write to database
                    try {
                        val dbHelper = DBHelper(ctx, null)
                        dbHelper.addRouter(BSSID, SSID, r, c)
                        dbHelper.close()
                    } catch (e: Exception) {
                        Toast.makeText(ctx,
                            "Could not add router",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            llRouters.addView(btn)
        }
    }

    private fun scanFailure(ctx: Context) {
        Toast.makeText(ctx,
            "Scan Failed",
            Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireContext().unregisterReceiver(wifiScanReceiver)
    }
}