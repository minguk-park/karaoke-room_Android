package com.example.bletest3

import android.Manifest
import java.util.*

class Constants {
    companion object {
        // used to identify adding bluetooth names
        const val REQUEST_ENABLE_BT = 1
        // used to request fine location permission
        const val REQUEST_ALL_PERMISSION = 2
        val PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        /*사용자 BLE UUID Service/Rx/Tx*/
        //const val SERVICE_STRING = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        const val SERVICE_STRING = "c6a89af5-0385-4d4a-8cb4-c856fcbf1320"
        //const val CHARACTERISTIC_COMMAND_STRING = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CHARACTERISTIC_COMMAND_STRING = "c6a89af5-0385-4d4a-8cb4-c856fcbf1321"
        const val CHARACTERISTIC_RESPONSE_STRING = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
        val SERVICE_UUID:UUID= UUID.fromString(SERVICE_STRING)
        //const val CONFIG_UUID = "00005609-0000-1001-8080-00705c9b34cb"
        /*const val SERVICE_STRING = "0000fff0-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_COMMAND_STRING = "0000fff1-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_RESPONSE_STRING = "0000fff2-0000-1000-8000-00805f9b34fb"*/
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
        //const val CLIENT_CHARACTERISTIC_CONFIG = "c6a89af5-0385-4d4a-8cb4-c856fcbf1321"
    }
}