package com.example.bletest2

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val TAG: String = "Central"
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2
    private val SCAN_PERIOD = 5000

    private var ble_adapter: BluetoothAdapter? = null
    private var is_scanning: Boolean = false
    private var connected: Boolean = false
    private var scan_results: Map<String, BluetoothDevice>? = null

    private var ble_scanner: BluetoothLeScanner? = null
    private var scan_handler: Handler? = Handler()

    var SERVICE_STRING = "0000aab0-f845-40fa-995d-658a43feea4c"
    var UUID_TDCS_SERVICE: UUID = UUID.fromString(SERVICE_STRING)
    var CHARACTERISTIC_COMMAND_STRING = "0000AAB1-F845-40FA-995D-658A43FEEA4C"
    var UUID_CTRL_COMMAND: UUID = UUID.fromString(CHARACTERISTIC_COMMAND_STRING)
    var CHARACTERISTIC_RESPONSE_STRING = "0000AAB2-F845-40FA-995D-658A43FEEA4C"
    var UUID_CTRL_RESPONSE: UUID = UUID.fromString(CHARACTERISTIC_RESPONSE_STRING)
    val MAC_ADDR = "64:69:4E:22:68:FB"      // 특정 MAC ADDRESS 스캔

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var ble_manager: BluetoothManager
        ble_manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        //set ble adapter
        ble_adapter = ble_manager.adapter

        val btnScan: Button = findViewById<Button>(R.id.btnScan)
        btnScan.setOnClickListener { v -> startScan(v) }
    }

    /* BLE 기능 미지원시 앱 종료*/
    override fun onResume() {
        super.onResume()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startScan(view: View) {
        val txtState = findViewById<TextView>(R.id.txtState)
        txtState.text = "Scanning..."
        if (ble_adapter == null || !ble_adapter!!.isEnabled) {
            requestEnableBLE()
            txtState.text = "Scanning Failed: ble not enabled"
            return;
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            txtState.text = "Scanning Failed: no fine Location permission"
            return;
        }
        val filters: MutableList<ScanFilter> = ArrayList()
        val scan_filter = ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR)
                .build()
        filters.add(scan_filter)
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
        scan_results = HashMap()
        val scan_cb = object : ScanCallback() {
            private var cb_scan_result: MutableMap<String, BluetoothDevice>? = null

            init {
                cb_scan_result = scan_results as HashMap<String, BluetoothDevice>
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Toast.makeText(applicationContext, "BLE Scan Failed : $errorCode", Toast.LENGTH_LONG).show()
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (result != null) {
                    addScanResult(result)
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                if (results != null) {
                    for (result in results) {
                        addScanResult(result)
                    }
                }
            }

            private fun addScanResult(result: ScanResult) {
                val device: BluetoothDevice = result.device
                val device_address: String = device.address
                cb_scan_result?.put(device_address, device)
                txtState.setText("add Scanned Device : $device_address")
            }
        }
        ble_scanner = ble_adapter!!.bluetoothLeScanner
        ble_scanner?.startScan(filters, settings, scan_cb)
        is_scanning = true
        //scan_handler!!.postDelayed(this::stopScan,SCAN_PERIOD.toLong())


    }

    private fun requestEnableBLE() {
        val ble_enable_intent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(ble_enable_intent, REQUEST_ENABLE_BT)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
    }


}
