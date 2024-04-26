package com.yangxiao.bluetoothtest.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modelbest.minicpmyx.ble.CharacteristicHandler
import com.welie.blessed.BluetoothPeripheral
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel : ViewModel() {
    val imageData = MutableStateFlow<ByteArray?>(null)

    fun loadImage(peripheral: BluetoothPeripheral, uuIDv4: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 假设getPicture是你的函数，用于从蓝牙设备获取图片数据
            val picData = getPicture(peripheral, uuIDv4)
            imageData.value = picData
        }
    }

    // 假设的获取图片数据方法
    suspend fun getPicture(peripheral: BluetoothPeripheral, id: String): ByteArray {
        var buffer = byteArrayOf()
        val uuidLength = 36 // The length of UUID in bytes
        val bleReadInterval = 15L
        val uuidByte = id.toByteArray();
        while (true) {
//            delay(bleReadInterval) // Simulating asynchronous read delay

            val result = peripheral.readCharacteristic(
                CharacteristicHandler.SERVICE_UUID,
                CharacteristicHandler.PICTURE_CHARACTERISTIC_UUID
            )
//        val data = characteristic.value ?: continue // or handle error

            if (result.copyOfRange(0, uuidLength).contentEquals(uuidByte)) {
                if (result.size == uuidByte.size) {
                    // UUID only, read completed
                    return buffer
                }

                // Concat buffer
                buffer += result.copyOfRange(uuidLength, result.size)
            } else {
                // UUID mismatch
                throw Exception("Error: Communication was interrupted.")
            }
        }
    }
}