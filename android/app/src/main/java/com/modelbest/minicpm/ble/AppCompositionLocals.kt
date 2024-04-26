package com.yangxiao.bluetoothtest.ble

import androidx.compose.runtime.staticCompositionLocalOf
import com.welie.blessed.BluetoothCentralManager

val LocalBluetoothCentralManager = staticCompositionLocalOf<BluetoothCentralManager> {
    error("BluetoothCentralManager not provided")
}