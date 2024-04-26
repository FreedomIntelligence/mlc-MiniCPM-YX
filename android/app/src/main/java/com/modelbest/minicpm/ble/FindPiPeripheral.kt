package com.yangxiao.bluetoothtest.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanSettings
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService


import com.welie.blessed.BluetoothPeripheral
import com.modelbest.minicpmyx.ble.CharacteristicHandler.Companion.SERVICE_UUID
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.M)
@RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
@Composable
internal fun FindPiPeripheralScreen(onConnect: (BluetoothPeripheral) -> Unit) {
    val context = LocalContext.current
    val adapter = checkNotNull(context.getSystemService<BluetoothManager>()?.adapter)

//    val central = remember { BluetoothCentralManager(context.applicationContext) }
    val central = LocalBluetoothCentralManager.current
    var scanning by remember {
        mutableStateOf(true)
    }
    val peripherals = remember {
        mutableStateListOf<BluetoothPeripheral>()
    }
//    val pairedDevices = remember {
//        // Get a list of previously paired devices
//        mutableStateListOf<BluetoothDevice>(*adapter.bondedDevices.toTypedArray())
//    }
//    val sampleServerDevices = remember {
//        mutableStateListOf<BluetoothDevice>()
//    }
    val scanSettings: ScanSettings = ScanSettings.Builder()
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .build()

    // This effect will start scanning for devices when the screen is visible
    // If scanning is stop removing the effect will stop the scanning.
    if (scanning) {


        central.scanForPeripheralsWithServices(arrayOf(SERVICE_UUID),
        { peripheral, scanResult ->
            Timber.i("Found peripheral '${peripheral.name}' with RSSI ${scanResult.rssi}")
            peripherals.add(peripheral)
            central.stopScan()
//            connectPeripheral(peripheral)
        },
            { scanFailure -> Timber.e("scan failed with reason $scanFailure") })

    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Available devices", style = MaterialTheme.typography.titleSmall)
            if (scanning) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(
                    onClick = {
                        peripherals.clear()
                        scanning = true
                    },
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (peripherals.isEmpty()) {
                item {
                    Text(text = "No devices found")
                }
            }
            items(peripherals) { item ->
                BluetoothPeripheralItem(
                    bluetoothPeripheral = item,
                    onConnect = onConnect,
                )
            }

//            if (pairedDevices.isNotEmpty()) {
//                item {
//                    Text(text = "Saved devices", style = MaterialTheme.typography.titleSmall)
//                }
//                items(pairedDevices) {
//                    BluetoothPeripheralItem(
//                        bluetoothPeripheral = it,
//                        onConnect = onConnect,
//                    )
//                }
//            }
        }
    }


}

@SuppressLint("MissingPermission")
@Composable
internal fun BluetoothPeripheralItem(
    bluetoothPeripheral: BluetoothPeripheral,
    onConnect: (BluetoothPeripheral) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onConnect(bluetoothPeripheral) },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
//        Text(
//            if (isSampleServer) {
//                "GATT Sample server"
//            } else {
//
//            },
//            bluetoothPeripheral.name ?: "N/A"
//            style = if (isSampleServer) {
//                TextStyle(fontWeight = FontWeight.Bold)
//            } else {
//                TextStyle(fontWeight = FontWeight.Normal)
//            },
//        )
        Text(text = bluetoothPeripheral.name ?: "N/A")
        Text(bluetoothPeripheral.address)
//        bluetoothPeripheral.bondState
//        val state = when (bluetoothPeripheral.bondState) {
//            bluetoothPeripheral.o .BOND_BONDED -> "Paired"
//            BluetoothDevice.BOND_BONDING -> "Pairing"
//            else -> "None"
//        }
//        Text(text = state)

    }
}