package com.iktwo.kodices

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Spike: proves a non-serializable marker type can ride along in a [SerializersModule] and be read
 * back off a [Json] instance. This is the transport the parser-scoped registry relies on, so it is
 * verified in isolation before anything depends on it.
 */
class ContextCarrierSpikeTest {
    private class Payload(
        val name: String,
    )

    private class Carrier(
        val payload: Payload,
    ) : KSerializer<Payload> {
        override val descriptor = buildClassSerialDescriptor("com.iktwo.kodices.SpikePayload")

        override fun serialize(
            encoder: Encoder,
            value: Payload,
        ): Nothing = error("not serializable")

        override fun deserialize(decoder: Decoder): Nothing = error("not serializable")
    }

    @Test
    fun aMarkerSerializerSurvivesJsonConstructionAndCanBeReadBack() {
        val json = Json {
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(Payload::class, Carrier(Payload("scoped")))
            }
        }

        // Constructing Json validates the module; if the descriptor were an error() this would throw.
        val carrier = json.serializersModule.getContextual(Payload::class) as? Carrier
        assertNotNull(carrier, "Contextual marker serializer was not retrievable")
        assertEquals("scoped", carrier.payload.name)
    }

    @Test
    fun eachJsonInstanceCarriesItsOwnPayload() {
        val base = Json { ignoreUnknownKeys = true }

        fun scoped(name: String) =
            Json(from = base) {
                serializersModule = base.serializersModule +
                    SerializersModule { contextual(Payload::class, Carrier(Payload(name))) }
            }

        val first = scoped("first")
        val second = scoped("second")

        assertEquals("first", (first.serializersModule.getContextual(Payload::class) as Carrier).payload.name)
        assertEquals("second", (second.serializersModule.getContextual(Payload::class) as Carrier).payload.name)
        assertEquals(null, base.serializersModule.getContextual(Payload::class))
    }
}
