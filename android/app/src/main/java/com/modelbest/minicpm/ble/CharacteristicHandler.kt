package com.modelbest.minicpmyx.ble

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.impl.NameBasedGenerator
import kotlinx.coroutines.flow.MutableStateFlow

class CharacteristicHandler {

    companion object {



        val dnsNamespaceUUID = NameBasedGenerator.NAMESPACE_DNS;
        val namespaceUUID = Generators.nameBasedGenerator(dnsNamespaceUUID).generate("discretetom.github.io");
        val SERVICE_UUID = Generators.nameBasedGenerator(namespaceUUID).generate("Omnitrix");

        val TEST_CHARACTERISTIC_UUID = Generators.nameBasedGenerator(SERVICE_UUID).generate("test");
        val PICTURE_CHARACTERISTIC_UUID = Generators.nameBasedGenerator(SERVICE_UUID).generate("picture");
        val AUDIO_CHARACTERISTIC_UUID = Generators.nameBasedGenerator(SERVICE_UUID).generate("audio");

//        val dnsNamespaceUUID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
//        val namespaceUUID = UUID.nameUUIDFromBytes("discretetom.github.io".toByteArray() + dnsNamespaceUUID.toString().toByteArray())
//        val SERVICE_UUID = UUID.nameUUIDFromBytes("Omnitrix".toByteArray() + namespaceUUID.toString().toByteArray())
//        val CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes("wifi".toByteArray() + SERVICE_UUID.toString().toByteArray())
        // Random UUID for our service known between the client and server to allow communication
//        val SERVICE_UUID: UUID = UUID.fromString("00002222-0000-1000-8000-00805f9b34fb")

        // Same as the service but for the characteristic
//        val CHARACTERISTIC_UUID: UUID = UUID.fromString("00001111-0000-1000-8000-00805f9b34fb")

        const val ACTION_START_ADVERTISING = "start_ad"
        const val ACTION_STOP_ADVERTISING = "stop_ad"

        // Important: this is just for simplicity, there are better ways to communicate between
        // a service and an activity/view
        val serverLogsState: MutableStateFlow<String> = MutableStateFlow("")
        val isServerRunning = MutableStateFlow(false)

        private const val CHANNEL = "gatt_server_channel"
    }

}