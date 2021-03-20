package com.example.karaoke1

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import java.util.*

class BluetoothUtils {
    companion object{
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
        fun findCommandCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic?{
            //Log.d(TAG,"findCommandCharacteristic")
            return findCharacteristic(gatt, Constants.CHARACTERISTIC_COMMAND_STRING)
            //return findCharacteristic(gatt, CLIENT_CHARACTERISTIC_CONFIG)
        }

        /*주변 디바이스의 응답 characteristic find*/
        fun findResponseCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic?{
            return findCharacteristic(gatt, Constants.CHARACTERISTIC_RESPONSE_STRING)
        }

        /*주어진 uuid characteristic find*/
        fun findCharacteristic(gatt: BluetoothGatt, uuidString: String): BluetoothGattCharacteristic?{
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
            val uuid: UUID =characteristic.uuid
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
            return matchUUIDs(serviceUuidString, Constants.SERVICE_STRING)
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
                Constants.CHARACTERISTIC_COMMAND_STRING,
                Constants.CHARACTERISTIC_RESPONSE_STRING,
                Constants.CLIENT_CHARACTERISTIC_CONFIG
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

        fun onClickWrite(mGatt:BluetoothGatt,commend:String){
            //Log.d(TAG,"OnClickWrite to $mGatt")
            val cmdCharacteristic= BluetoothUtils.findCommandCharacteristic((mGatt!!))
            //val cmdCharacteristic=mGatt!!.services
            //val cmdCharacteristic= BluetoothUtils.findCharacteristic((mGatt!!), CHARACTERISTIC_COMMAND_STRING)
            //txtState.text="write 0x12"
            if(cmdCharacteristic==null){
                Log.e("Central", "Unable to find cmd characteristic $cmdCharacteristic")
                //disconnectGattServer()
                return
            }
            //"text" 보내기
            val cmdBytes = ByteArray(256)
            val text="test"
            Log.d("Central","$cmdBytes // cmdBytes")
            cmdCharacteristic.value=text.toByteArray()
            //Log.d(TAG,"$cmdBytes")
            val success: Boolean = mGatt!!.writeCharacteristic(cmdCharacteristic)
            // check the result
            if( !success ) {
                Log.e("Central", "Failed to write command")
            }
            Log.d("Central","success : $success")
        }
    }
}