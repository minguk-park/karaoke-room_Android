package com.example.bletest3

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.bluetooth.le.ScanFilter.Builder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Observable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.ObservableField
import com.example.bletest3.BluetoothUtils.Companion.findResponseCharacteristic
import com.example.bletest3.Constants.Companion.CLIENT_CHARACTERISTIC_CONFIG
import com.example.bletest3.Constants.Companion.PERMISSIONS
import com.example.bletest3.Constants.Companion.REQUEST_ALL_PERMISSION
import com.example.bletest3.Constants.Companion.REQUEST_ENABLE_BT
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    private val SCAN_PERIOD = 5000
    private val TAG="STATE"
    var _txtRead: String = ""
    var txtRead : ObservableField<String> = ObservableField("")


    private var mScanning = false
    private var mHandler: Handler? = null
    private val mLogHandler: Handler? = null
    private var ScanResults: ArrayList<BluetoothDevice>? = ArrayList()

    private var mConnected:Boolean=false
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mGatt: BluetoothGatt? = null

    var isScanning:Boolean=false

    val MAC_ADDR = "64:69:4E:22:68:FB"
    //val MAC_ADDR= "B8:27:EB:2F:08:1D"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter


        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }
    }

    fun onClickScan(){
        //if(!isScanning.get()){
            if(mBluetoothAdapter==null||!mBluetoothAdapter!!.isEnabled){
                requestBluetoothEnable()
                //txtState.text="Scanning Failed: Ble not Enabled"
                return
            }
            //scan filter
        val filters:MutableList<ScanFilter> = ArrayList()
        val scanFilter: ScanFilter= ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR)
                .build()
        filters.add(scanFilter)
        val settings=ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
        mBluetoothAdapter?.bluetoothLeScanner?.startScan(BLEScanCallback)
        //txtState.text="Scanning..."
        isScanning=true
        //connectDevice(ScanResults!!.get(0))
        Timer("SettingUp",false).schedule(3000){stopScan()}

        //}
    }

    private fun stopScan(){
        mBluetoothAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        isScanning=false
        //btnScan.text="Start Scan"
        connectDevice(ScanResults!![0])
        ScanResults=ArrayList()
    }

    private val BLEScanCallback:ScanCallback=object:ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addScanResult(result)
        }
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            //super.onBatchScanResults(results)
            for(result in results){
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }

        private fun addScanResult(result:ScanResult){
            val device=result.device
            val deviceAddress=device.address
            ScanResults?.add(result.device)
            //txtState.setText("add Scanned device: $deviceAddress")
        }
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

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        //log("Requested user enables Bluetooth. Try starting the scan again.")
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

   /*@RequiresApi(Build.VERSION_CODES.M)
    private fun hasLocationPermissions(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }*/

    fun connectDevice(device:BluetoothDevice?){
        //txtState.setText("Connecting to $device?.address")
        mGatt=device?.connectGatt(application,false,gattClientcallback)
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
                gatt!!.discoverServices()
            }else if(newState==BluetoothProfile.STATE_DISCONNECTED){
                disconnectGattServer()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            /*discover failure*/
            if(status!=BluetoothGatt.GATT_SUCCESS){
                return
            }

            // command characteristic을 GATT 서버로부터 찾음
            val respCharacteristic = gatt?.let { findResponseCharacteristic(it) }
            if(respCharacteristic==null){
                disconnectGattServer()
                return
            }
            gatt.setCharacteristicNotification(respCharacteristic,true)

            // UUID for notification
            val descriptor:BluetoothGattDescriptor=respCharacteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value=BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)

        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if(status==BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG,"Characteristic written successfully")
            }else{
                Log.e(TAG,"Characteristic write unsuccessful, status=$status")
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
            txtRead.set(_txtRead)
        }
    }


    fun setConnected(connected : Boolean){
        mConnected=connected
    }

    fun disconnectGattServer(){
        mConnected=false
        if(mGatt!=null){
            mGatt!!.disconnect()
            mGatt!!.close()
        }
    }

    fun onClickWrite(){
        val cmdCharacteristic=BluetoothUtils.findCommandCharacteristic((mGatt!!))
        if(cmdCharacteristic==null){
            disconnectGattServer()
            return
        }
        //byte 0x12 보내기
        val cmdBytes = ByteArray(2)
        cmdBytes[0]=1
        cmdBytes[1] = 2
        cmdCharacteristic.value=cmdBytes
    }



}