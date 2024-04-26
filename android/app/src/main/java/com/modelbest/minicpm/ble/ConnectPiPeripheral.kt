package com.yangxiao.bluetoothtest.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.platform.connectivity.bluetooth.ble.BluetoothSampleBox
import com.fasterxml.uuid.Generators
import com.modelbest.minicpmyx.AppViewModel
import com.modelbest.minicpmyx.MainActivity
import com.modelbest.minicpmyx.MessageRole
import com.modelbest.minicpmyx.ResizeBitmap
import com.modelbest.minicpmyx.SliceImage
import com.modelbest.minicpmyx.bitmapToBytes
import com.modelbest.minicpmyx.ble.CharacteristicHandler.Companion.AUDIO_CHARACTERISTIC_UUID
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.ConnectionFailedException
import com.welie.blessed.WriteType
import com.modelbest.minicpmyx.ble.CharacteristicHandler.Companion.PICTURE_CHARACTERISTIC_UUID
import com.modelbest.minicpmyx.ble.CharacteristicHandler.Companion.SERVICE_UUID
import com.welie.blessed.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.StandardCharsets




@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun ConnectPi(chatState: AppViewModel.ChatState, activity: Activity) {
    var selectedDevice by remember {
        mutableStateOf<BluetoothPeripheral?>(null)
    }

    (activity as MainActivity).chatState = chatState

    // Check that BT permissions and that BT is available and enabled
    BluetoothSampleBox {
        AnimatedContent(targetState = selectedDevice, label = "Selected device") { peripheral ->
            if (peripheral == null) {
                // Scans for BT devices and handles clicks (see FindDeviceSample)
                FindPiPeripheralScreen {
                    selectedDevice = it
                }

            } else {
                // Once a device is selected show the UI and try to connect device
                ConnectPiScreen(peripheral = peripheral, chatState=chatState, activity = activity) {
                    selectedDevice = null
                }
            }
        }
    }
}


@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ConnectPiScreen(peripheral: BluetoothPeripheral,  chatState: AppViewModel.ChatState, activity: Activity, onClose: () -> Unit) {

    val scope = rememberCoroutineScope()
    val central = LocalBluetoothCentralManager.current

    var local_activity : MainActivity = activity as MainActivity


    var connectionState by remember() {
        mutableStateOf<ConnectionState>(ConnectionState.DISCONNECTED)
    }

    var mtu by remember {
        mutableStateOf(-1)
    }

    var uuIDv4 by remember() {
        mutableStateOf<String>("")
    }

    val viewModel: BluetoothViewModel = viewModel()
    val imageData by viewModel.imageData.collectAsState()
    val imageBitmap = imageData?.let {
        BitmapFactory.decodeByteArray(it, 0, it.size)
    }


    central.observeConnectionState { newPeripheral, state ->
        Timber.i("Peripheral ${newPeripheral.name} has $state")
        connectionState = state
    }


//    var firstMessageText by remember() {
//        mutableStateOf<String>("no text")
//    }
//
//
//    LaunchedEffect(chatState.messages) {
//        for(message in chatState.messages){
//            if (message.role == MessageRole.Bot){
//                firstMessageText = message.text
//                break
//            }
//        }
//    }

    val firstBotMessageText = chatState.messages.find { it.role == MessageRole.Bot }?.text ?: "no text"


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Devices details", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Name: ${peripheral.name} (${peripheral.address})")
        Text(text = "Status: $connectionState")
        Text(text = "MTU: ${mtu}")
        Text(
            text = "Image handle state: ${chatState.report.value}" ,
        )
        Text(text = "Picture Description: ${firstBotMessageText}")
//        Column(
//
//
//        ) {
//            items(
//                items = chatState.messages,
//                key = { message -> message.id },
//            ) { message ->
//                if (message.role == MessageRole.Bot){
//                    Text(text = "Picture Description: ${message.text}")
//                }
////                MessageView(messageData = message, activity)
//            }
//
//        }
//        Text(text = "Services: ${state?.services?.joinToString { it.uuid.toString() + " " + it.type }}")
//        Text(text = "Message sent: ${state?.messageSent}")
//        Text(text = "Message received: ${state?.messageReceived}")
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        central.connectPeripheral(peripheral)
                    } catch (connectionFailed: ConnectionFailedException) {
                        Timber.e("connection failed")
                    }
                    mtu = peripheral.requestMtu(BluetoothPeripheral.MAX_MTU)

                }
            },
        ) {
            Text(text = "Connect Pi And Request MTU")
        }
//        Button(
//            onClick = {
//                scope.launch(Dispatchers.IO) {
////                    if (state?.connectionState == BluetoothProfile.STATE_DISCONNECTED) {
////                        state?.gatt?.connect()
////                    }
////                    // Example on how to request specific MTUs
////                    // Note that from Android 14 onwards the system will define a default MTU and
////                    // it will only be sent once to the peripheral device
//////                    state?.gatt?.requestMtu(Random.nextInt(27, 190))
////                    state?.gatt?.requestMtu(512)
//                    mtu = peripheral.requestMtu(BluetoothPeripheral.MAX_MTU)
//                }
//            },
//        ) {
//            Text(text = "Request MTU")
//        }
        Button(
            enabled = connectionState == ConnectionState.CONNECTED,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    uuIDv4 = Generators.randomBasedGenerator().generate().toString()
                    takePicture(peripheral, uuIDv4)
//                    delay(500L)
//                    viewModel.loadImage(peripheral, uuIDv4)
                }
            },
        ) {
            Text(text = "Take Picture")
        }
        Button(
//            enabled = state?.gatt != null && pictureCharacteristic != null,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    viewModel.loadImage(peripheral, uuIDv4)
                }
            },
        ) {
            Text(text = "Get picture")
        }
        Button(
//            enabled = state?.gatt != null && pictureCharacteristic != null,
            onClick = {
                val slice_result = imageBitmap?.let { SliceImage(it, never_split = true) }
                if (slice_result != null) {
                    var height = 448
                    var width = 448
                    var image_datas = arrayOf<IntArray>()
                    var image = ResizeBitmap(slice_result.image, width, height)
                    var image_data = bitmapToBytes(image)
                    image_datas += image_data
                    Log.v("image size = ", image.height.toString() + ", " + image.width.toString())

                    var steps : Int = 0
                    for (image in slice_result.patchs){
                        var resize_image = ResizeBitmap(image, width, height)
                        image_datas += bitmapToBytes(resize_image)
                        Log.v("requestImage ", steps.toString())
                    }
                    var best_grid = slice_result.best_grid

                    println("image_datas.size: "+image_datas.size)
                    println("image_datas.size: "+image_datas[0].size)

                    local_activity.chatState.requestImage(image_datas, height, width, best_grid[0])
                    local_activity.slice_nums = slice_result.patchs.size
                }
            },
        ) {
            Text(text = "LLM handle picture")
        }
        Button(
//            enabled = state?.gatt != null && pictureCharacteristic != null,
            onClick = {
                var text = "请给我介绍一下图片"
                chatState.requestGenerate(text, local_activity.slice_nums)
            },
        ) {
            Text(text = "LLM handle pic to text")
        }
        Button(
//            enabled = state?.gatt != null && pictureCharacteristic != null,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    playAudio(peripheral, firstBotMessageText)
                }
            },
        ) {
            Text(text = "Play Audio")
        }

        imageData?.let {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

//        Button(
//            enabled = state?.gatt != null,
//            onClick = {
//                scope.launch(Dispatchers.IO) {
//                    // Once we have the connection discover the peripheral services
//                    state?.gatt?.discoverServices()
//                    peripheral.services
//                }
//            },
//        ) {
//            Text(text = "Discover")
//        }
//        Button(
//            enabled = state?.gatt != null && characteristic != null,
//            onClick = {
//                scope.launch(Dispatchers.IO) {
//                    sendData(state?.gatt!!, characteristic!!)
//                }
//            },
//        ) {
//            Text(text = "Write to server")
//        }
//        Button(
//            enabled = state?.gatt != null && characteristic != null,
//            onClick = {
//                scope.launch(Dispatchers.IO) {
//                    state?.gatt?.readCharacteristic(characteristic)
//                }
//            },
//        ) {
//            Text(text = "Read characteristic")
//        }


        Button(onClick = onClose) {
            Text(text = "Close")
        }
    }
}


suspend fun takeAndGetPicture(peripheral: BluetoothPeripheral){
    val content = "a"


    val uuid = Generators.randomBasedGenerator().generate()
    val uuidBytes = uuid.toString().toByteArray(StandardCharsets.UTF_8)
    val contentBytes = content.toByteArray(StandardCharsets.UTF_8)

    val bytesToSend = uuidBytes + contentBytes

    peripheral.writeCharacteristic(SERVICE_UUID, PICTURE_CHARACTERISTIC_UUID, bytesToSend, WriteType.WITH_RESPONSE)
}

suspend fun takePicture(peripheral: BluetoothPeripheral, id: String){
//    val content = "hello"
    val uuidBytes = id.toByteArray(StandardCharsets.UTF_8)
//    val contentBytes = content.toByteArray(StandardCharsets.UTF_8)
    val bytesToSend = uuidBytes

    peripheral.writeCharacteristic(SERVICE_UUID, PICTURE_CHARACTERISTIC_UUID, bytesToSend, WriteType.WITH_RESPONSE)
}


suspend fun playAudio(peripheral: BluetoothPeripheral, answer: String){
//    val content = "a"

    val uuid = Generators.randomBasedGenerator().generate()
    val uuidBytes = uuid.toString().toByteArray(StandardCharsets.UTF_8)
    val contentBytes = answer.toByteArray(StandardCharsets.UTF_8)

    val bytesToSend = uuidBytes + contentBytes

    peripheral.writeCharacteristic(SERVICE_UUID, AUDIO_CHARACTERISTIC_UUID, bytesToSend, WriteType.WITH_RESPONSE)

    peripheral.writeCharacteristic(SERVICE_UUID, AUDIO_CHARACTERISTIC_UUID, uuidBytes, WriteType.WITH_RESPONSE)

}

//suspend fun getPicture(peripheral: BluetoothPeripheral, id: String): ByteArray {
//    var buffer = byteArrayOf()
//    val uuidLength = 36 // The length of UUID in bytes
//    val bleReadInterval = 15L
//    val uuidByte = id.toByteArray();
//    while (true) {
//        delay(bleReadInterval) // Simulating asynchronous read delay
//
//        val result = peripheral.readCharacteristic(SERVICE_UUID, PICTURE_CHARACTERISTIC_UUID)
////        val data = characteristic.value ?: continue // or handle error
//
//        if (result.copyOfRange(0, uuidLength).contentEquals(uuidByte)) {
//            if (result.size == uuidByte.size) {
//                // UUID only, read completed
//                return buffer
//            }
//
//            // Concat buffer
//            buffer += result.copyOfRange(uuidLength, result.size)
//        } else {
//            // UUID mismatch
//            throw Exception("Error: Communication was interrupted.")
//        }
//    }
//}

@Composable
fun BluetoothImageView(imageDataFlow: MutableStateFlow<ByteArray?>) {
    val imageData = imageDataFlow.collectAsState(initial = null).value
    imageData?.let {
        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth())
    }
}