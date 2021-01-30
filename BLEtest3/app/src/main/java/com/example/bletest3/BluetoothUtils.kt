package com.example.bletest3

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.example.bletest3.Constants.Companion.CHARACTERISTIC_COMMAND_STRING
import com.example.bletest3.Constants.Companion.CHARACTERISTIC_RESPONSE_STRING
import com.example.bletest3.Constants.Companion.SERVICE_STRING
import java.util.*
import kotlin.collections.ArrayList



class BluetoothUtils {
    companion object{
        /*Find characteristics of BLE*/
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
        fun findCommandCharacteristic(gatt:BluetoothGatt):BluetoothGattCharacteristic?{
            return findCharacteristic(gatt, CHARACTERISTIC_COMMAND_STRING)
        }

        /*주변 디바이스의 응답 characteristic find*/
        fun findResponseCharacteristic(gatt:BluetoothGatt):BluetoothGattCharacteristic?{
            return findCharacteristic(gatt, CHARACTERISTIC_RESPONSE_STRING)
        }

        /*주어진 uuid characteristic find*/
        private fun findCharacteristic(gatt: BluetoothGatt, uuidString:String):BluetoothGattCharacteristic?{
            val serviceList=gatt.services
            val service= findGattService(serviceList)?:return null
            val characteristicList=service.characteristics
            for(characteristic in characteristicList){
                if(matchCharacteristic(characteristic,uuidString)){
                    return characteristic
                }
            }
            return null
        }

        /*주어진 characteristic과 uuid 문자열의 Match 확인*/
        private fun matchCharacteristic(characteristic: BluetoothGattCharacteristic?,uuidString: String):Boolean{
            if(characteristic==null){
                return false
            }
            val uuid:UUID=characteristic.uuid
            return matchUUIDs(uuid.toString(),uuidString)
        }

        /*서버의 서비스와 일치하는 Gatt service를 찾음*/
        private fun findGattService(serviceList : List<BluetoothGattService>) : BluetoothGattService?{
            for(service in serviceList){
                val serviceUuidString=service.uuid.toString()
                if(matchServiceUUIDString(serviceUuidString)){
                    return service
                }
            }
            return null
        }

        /*uuid와 service uuid 매칭*/
        private fun matchServiceUUIDString(serviceUuidString:String):Boolean{
            return matchUUIDs(serviceUuidString,SERVICE_STRING)
        }
        /*characteristic 매칭 여부 체크*/
        private fun isMatchingCharacteristic(characteristic : BluetoothGattCharacteristic?):Boolean{
            if(characteristic==null){
                return false
            }
            val uuid: UUID =characteristic.uuid
            return matchCharacteristicUUID(uuid.toString())
        }

        /*서버 characteristic의 uuid를 쿼리*/
        private fun matchCharacteristicUUID(characteristicUuidString:String):Boolean{
            return matchUUIDs(characteristicUuidString, CHARACTERISTIC_COMMAND_STRING, CHARACTERISTIC_RESPONSE_STRING)
        }

        /*Try to match a uuid with the given set of uuid*/
        private fun matchUUIDs(uuidString:String,vararg matches : String):Boolean{
            for(match in matches){
                if(uuidString.equals(match,ignoreCase = true)){
                    return true
                }
            }
            return false
        }
    }
}