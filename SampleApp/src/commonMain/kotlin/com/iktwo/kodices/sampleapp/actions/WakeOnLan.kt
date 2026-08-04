package com.iktwo.kodices.sampleapp.actions

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.Source

object WakeOnLan {
    /**
     * Sends a Wake-on-LAN magic packet to [macAddress] over [broadcastIP].
     *
     * Suspending rather than blocking: this used to wrap the socket work in `runBlocking`, and it is
     * called from an action handler on the UI thread, so an unreachable host froze the app.
     *
     * Throws if the MAC address is malformed or the packet cannot be sent; the caller decides how to
     * surface that.
     */
    suspend fun wakeDevice(
        macAddress: String,
        broadcastIP: String,
        port: Int = 9,
    ) {
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

        withContext(Dispatchers.IO) {
            SelectorManager(Dispatchers.IO).use { selectorManager ->
                aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 0)) {
                    broadcast = true
                }.use { socket ->
                    val source: Source = Buffer().apply { write(bytes) }

                    socket.send(
                        Datagram(
                            source,
                            InetSocketAddress(broadcastIP, port),
                        ),
                    )
                }
            }
        }
    }
}
