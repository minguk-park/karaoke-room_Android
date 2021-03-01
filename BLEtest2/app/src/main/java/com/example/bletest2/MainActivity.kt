package com.example.bletest2

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.bletest3.Constants.Companion.CHARACTERISTIC_COMMAND_STRING
import com.example.bletest3.Constants.Companion.CHARACTERISTIC_RESPONSE_STRING
import com.example.bletest3.Constants.Companion.CLIENT_CHARACTERISTIC_CONFIG
import com.example.bletest3.Constants.Companion.PERMISSIONS
import com.example.bletest3.Constants.Companion.REQUEST_ALL_PERMISSION
import com.example.bletest3.Constants.Companion.SERVICE_STRING
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val TAG: String = "Central"
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2
    private val SCAN_PERIOD = 5000
    var _txtRead: String = ""

    private var ble_adapter: BluetoothAdapter? = null
    private var is_scanning: Boolean = false
    private var connected: Boolean = false
    //private var scan_results: HashMap<String, BluetoothDevice>? = null
    private var scan_results: HashMap<String, BluetoothDevice>? =HashMap()
    private var scanDevice:BluetoothDevice?=null

    private var ble_scanner: BluetoothLeScanner? = null
    private var scan_handler: Handler? = Handler()
    private var mGatt: BluetoothGatt? = null
    private var mConnected:Boolean=false

    /*var SERVICE_STRING = "0000aab0-f845-40fa-995d-658a43feea4c"
    var UUID_TDCS_SERVICE: UUID = UUID.fromString(SERVICE_STRING)
    var CHARACTERISTIC_COMMAND_STRING = "0000AAB1-F845-40FA-995D-658A43FEEA4C"
    var UUID_CTRL_COMMAND: UUID = UUID.fromString(CHARACTERISTIC_COMMAND_STRING)
    var CHARACTERISTIC_RESPONSE_STRING = "0000AAB2-F845-40FA-995D-658A43FEEA4C"
    var UUID_CTRL_RESPONSE: UUID = UUID.fromString(CHARACTERISTIC_RESPONSE_STRING)*/
    val MAC_ADDR = "B8:27:EB:2F:08:1D"      // 특정 MAC ADDRESS 스캔

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }

        var ble_manager: BluetoothManager
        ble_manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        //set ble adapter
        ble_adapter = ble_manager.adapter

        val btnScan: Button = findViewById<Button>(R.id.btnScan)
        //val btnchk : Button= findViewById<Button>(R.id.btnchk)
        btnScan.setOnClickListener { v -> startScan(v) }

        btnconnect.setOnClickListener {
            ////Toast.makeText(applicationContext,"device : ${scanDevice?.get(0)}",Toast.LENGTH_LONG)
            Log.d(TAG,"connect $scanDevice")
            connectDevice(scanDevice)
        }
        btnSend.setOnClickListener { v->onClickWrite(v) }
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
            //Log.d(TAG,"Scanning Failed: ble not enabled")
            return;
        }
        /*if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            txtState.text = "Scanning Failed: no fine Location permission"
            Log.d(TAG,"Scanning Failed: no fine Location permission")
            return;
        }*/
        val filters: MutableList<ScanFilter> = ArrayList()
        val scan_filter = ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR)
                .build()
        filters.add(scan_filter)
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
        //scan_results = HashMap()

        ble_scanner = ble_adapter!!.bluetoothLeScanner
        ble_scanner?.startScan(filters, settings, scan_cb)
        is_scanning = true
        //txtState.setText("add Scanned Device : ${scanDevice?.get(0)}")
        //scan_handler!!.postDelayed(this::stopScan,SCAN_PERIOD.toLong())
        Handler().postDelayed({
            txtState.setText("add Scanned Device : ${scanDevice}")
            Log.d("TAG", "Stop Scanning")
            stopScan()
        }, 10000L)
    }

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
                Log.d(TAG, "onScanResult")
                addScanResult(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            if (results != null) {
                for (result in results) {
                    Log.d(TAG, "onBatchScanResults")
                    addScanResult(result)
                }
            }
        }
        private fun addScanResult(result: ScanResult) {
            Log.d(TAG, "add Scan Result")
            val device: BluetoothDevice = result.device
            val device_address: String = device.address
            scan_results?.put(device_address, device)
            scanDevice=device
            Log.d(TAG, "$device  $device_address  ${scan_results?.get(device)} ${scanDevice.toString()}")
            //txtState.setText("add Scanned Device : $device // ${scanDevice}")
        }
    }

    private fun requestEnableBLE() {
        val ble_enable_intent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(ble_enable_intent, REQUEST_ENABLE_BT)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun stopScan(){
        ble_adapter?.bluetoothLeScanner?.stopScan(scan_cb)
        is_scanning=false
        //btnScan.text="Start Scan"
        //connectDevice(ScanResults!![0])
        scan_results= HashMap()
    }

    private val gattClientcallback:BluetoothGattCallback=object:BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if(status==BluetoothGatt.GATT_FAILURE){
                disconnectGattServer()
                return
            }else if(status!=BluetoothGatt.GATT_SUCCESS){
                disconnectGattServer()
                return
            }
            if(newState==BluetoothProfile.STATE_CONNECTED){
                setConnected(true)
                Log.d(TAG, "Connected to the GATT server")
                gatt!!.discoverServices()
            }else if(newState==BluetoothProfile.STATE_DISCONNECTED){
                disconnectGattServer()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt,status)
            val services: List<BluetoothGattService>?=gatt!!.services
            lateinit var characteristics: List<BluetoothGattCharacteristic>
            lateinit var descriptors: List<BluetoothGattDescriptor>

            /*discover failure*/
            if(status!=BluetoothGatt.GATT_SUCCESS){
                Log.e(TAG, "Device service discovery failed, status: $status")
                return
            }
            Log.d(TAG, "Services discovery is successful")

            // command characteristic을 GATT 서버로부터 찾음
            /*val respCharacteristic = gatt?.let { findCommandCharacteristic(it) }
            //val respCharacteristic= BluetoothUtils.findBLECharacteristics(gatt!!)
            if(respCharacteristic==null){
                Log.d(TAG, "respCharacteristic failure")
                disconnectGattServer()
                return
            }*/
            if(services!=null) {
                //Log.d(TAG, "Services get ${services}")
                for (service in services) {
                    //Log.d(TAG, "Service")
                    characteristics=service.characteristics
                    for(characteristic in characteristics){
                        //Log.d(TAG, "${characteristic}")
                        descriptors=characteristic.descriptors
                        for(descriptor in descriptors){
                            //Log.d(TAG, "${descriptor.uuid.toString()}")
                            if(descriptor.uuid.toString().equals(CLIENT_CHARACTERISTIC_CONFIG)){
                                //Log.d(TAG, "Services get ${service.uuid}")
                                mGatt!!.setCharacteristicNotification(characteristic,true)
                                descriptor.value=BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                            }
                        }
                    }
                }
                Log.d(TAG, "Services get ${mGatt}")
            }
            /*gatt.setCharacteristicNotification(respCharacteristic, true)

            // UUID for notification
            val descriptor:BluetoothGattDescriptor=respCharacteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value=BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
            Log.d(TAG, "Services get ${respCharacteristic}")
            Log.d(TAG, "Services get ${mGatt}")*/
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if(status==BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG, "Characteristic written successfully")
            }else{
                Log.e(TAG, "Characteristic write unsuccessful, status=$status")
                disconnectGattServer()
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successfully")
                readCharacteristic(characteristic)
            } else {
                Log.e(TAG, "Characteristic read unsuccessful, status: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            readCharacteristic(characteristic)
        }

        private fun readCharacteristic(characteristic: BluetoothGattCharacteristic){
            val msg=characteristic.getStringValue(0)
            _txtRead+=msg
            //txtRead.set(_txtRead)
        }
    }

    fun setConnected(connected: Boolean){
        mConnected=connected
    }

    fun connectDevice(device: BluetoothDevice?){
        //txtState.setText("Connecting to $device?.address")
        Log.d(TAG,"Connecting to $device // $scanDevice")
        mGatt=device?.connectGatt(applicationContext, false, gattClientcallback)
    }

    fun disconnectGattServer(){
        Log.d(TAG, "Closing Gatt connection")
        mConnected=false
        if(mGatt!=null){
            mGatt!!.disconnect()
            mGatt!!.close()
        }
    }
    fun onClickWrite(view: View){
        //Log.d(TAG,"OnClickWrite to $mGatt")
        val cmdCharacteristic= findCommandCharacteristic((mGatt!!))
        //val cmdCharacteristic=mGatt!!.services
        //val cmdCharacteristic= BluetoothUtils.findCharacteristic((mGatt!!), CHARACTERISTIC_COMMAND_STRING)
        //txtState.text="write 0x12"
        if(cmdCharacteristic==null){
            Log.e(TAG, "Unable to find cmd characteristic $cmdCharacteristic")
            disconnectGattServer()
            return
        }
        //"text" 보내기
        val cmdBytes = ByteArray(256)
        val text="test"
        cmdBytes[0]=1
        cmdBytes[1] = 2
        Log.d(TAG,"$cmdBytes // cmdBytes")
        cmdCharacteristic.value=text.toByteArray()
        //Log.d(TAG,"$cmdBytes")
        val success: Boolean = mGatt!!.writeCharacteristic(cmdCharacteristic)
        // check the result
        if( !success ) {
            Log.e(TAG, "Failed to write command")
        }
        Log.d(TAG,"success : $success")
    }

    fun findBLECharacteristics(bluetoothGatt: BluetoothGatt) : List<BluetoothGattCharacteristic>{

        val matchingCharacteristics: MutableList<BluetoothGattCharacteristic> = ArrayList()
        val serviceList = bluetoothGatt.services
        val service=findGattService(serviceList)?:return matchingCharacteristics
        val characteristicList=service.characteristics
        for(characteristic in characteristicList){
            if(isMatchingCharacteristic(characteristic)){
                matchingCharacteristics.add(characteristic)
            }
        }
        return matchingCharacteristics
    }

    /*주변 디바이스의 commend characteristic find*/
    fun findCommandCharacteristic(gatt: BluetoothGatt):BluetoothGattCharacteristic?{
        //Log.d(TAG,"findCommandCharacteristic")
        return findCharacteristic(gatt, CHARACTERISTIC_COMMAND_STRING)
        //return findCharacteristic(gatt, CLIENT_CHARACTERISTIC_CONFIG)
    }

    /*주변 디바이스의 응답 characteristic find*/
    fun findResponseCharacteristic(gatt: BluetoothGatt):BluetoothGattCharacteristic?{
        return findCharacteristic(gatt, CHARACTERISTIC_RESPONSE_STRING)
    }

    /*주어진 uuid characteristic find*/
    fun findCharacteristic(gatt: BluetoothGatt, uuidString: String):BluetoothGattCharacteristic?{
        //Log.d(TAG,"findCharacteristic")
        val serviceList=gatt.services
        val service= findGattService(serviceList)?:return null
        //val service= serviceList?.let { findGattService(it) } ?:return null
        val characteristicList=service.characteristics
        for(characteristic in characteristicList){
            if(matchCharacteristic(characteristic, uuidString)){
                return characteristic
            }
        }
        return null
    }

    /*주어진 characteristic과 uuid 문자열의 Match 확인*/
    private fun matchCharacteristic(
            characteristic: BluetoothGattCharacteristic?,
            uuidString: String):Boolean{
        if(characteristic==null){
            return false
        }
        val uuid:UUID=characteristic.uuid
        return matchUUIDs(uuid.toString(), uuidString)
    }

    /*서버의 서비스와 일치하는 Gatt service를 찾음*/
    private fun findGattService(serviceList: List<BluetoothGattService>) : BluetoothGattService?{
        //Log.d(TAG,"findGattService / $serviceList")

        for(service in serviceList){
            val serviceUuidString=service.uuid.toString()
            //Log.d(TAG,"findGattService : serviceUuidString=$serviceUuidString")
            if(matchServiceUUIDString(serviceUuidString)){
                return service
            }
        }
        return null
    }

    /*uuid와 service uuid 매칭*/
    private fun matchServiceUUIDString(serviceUuidString: String):Boolean{
        return matchUUIDs(serviceUuidString, SERVICE_STRING)
    }
    /*characteristic 매칭 여부 체크*/
    private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?):Boolean{
        if(characteristic==null){
            return false
        }
        val uuid: UUID =characteristic.uuid
        return matchCharacteristicUUID(uuid.toString())
    }

    /*서버 characteristic의 uuid를 쿼리*/
    private fun matchCharacteristicUUID(characteristicUuidString: String):Boolean{
        //Log.d(TAG,"$characteristicUuidString // $CHARACTERISTIC_COMMAND_STRING // $CHARACTERISTIC_RESPONSE_STRING // $CLIENT_CHARACTERISTIC_CONFIG")
        return matchUUIDs(
                characteristicUuidString,
                CHARACTERISTIC_COMMAND_STRING,
                CHARACTERISTIC_RESPONSE_STRING,
                CLIENT_CHARACTERISTIC_CONFIG
        )
    }

    /*Try to match a uuid with the given set of uuid*/
    private fun matchUUIDs(uuidString: String, vararg matches: String):Boolean{
        //Log.d(TAG,"${matches}")
        for(match in matches){
            if(uuidString.equals(match, ignoreCase = true)){
                return true
            }
        }
        return false
    }
}
