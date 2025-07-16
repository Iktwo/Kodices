package com.iktwo.kodices.sampleapp.actions

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.Source

object WakeOnLan {
    fun wakeDevice(
        macAddress: String,
        broadcastIP: String,
        port: Int = 9,
    ) {
        try {
            val macAddressBytes = macAddress.split(":").map { it.toInt(16).toByte() }.toByteArray()
            val bytes = ByteArray(6 + macAddressBytes.size * 16)
            for (i in 0..5) {
                bytes[i] = 0xff.toByte()
            }
            for (i in 0..15) {
                macAddressBytes.copyInto(
                    bytes,
                    6 + (i * macAddressBytes.size),
                    0,
                    macAddressBytes.size,
                )
            }

            runBlocking {
                val selectorManager = SelectorManager(Dispatchers.IO)

                val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 0)) {
                    broadcast = true
                }

                val source: Source = Buffer().apply { write(bytes) }

                val packet = Datagram(
                    source,
                    InetSocketAddress(broadcastIP, port),
                )

                socket.send(
                    packet,
                )
            }
        } catch (e: Exception) {
            println("Failed to send Wake-on-LAN packet: $e")
            e.printStackTrace()
        }
    }
}
