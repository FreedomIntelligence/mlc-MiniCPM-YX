package com.modelbest.minicpmyx

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.welie.blessed.BluetoothCentralManager
import com.yangxiao.bluetoothtest.ble.ConnectPi
import com.yangxiao.bluetoothtest.ble.LocalBluetoothCentralManager


@Composable
fun BleView(navController: NavController, chatState: AppViewModel.ChatState, activity: Activity) {
    val context = LocalContext.current
    val central = remember { BluetoothCentralManager(context.applicationContext) }


    (activity as MainActivity).chatState = chatState

    CompositionLocalProvider(LocalBluetoothCentralManager provides central) {
        // 你的App结构
        ConnectPi(chatState = chatState, activity)
    }
}