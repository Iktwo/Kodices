package com.iktwo.kodices.actions

import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * Interface that represents an action that can be performed.
 */
@Serializable(with = Action.Companion::class)
interface Action {
    val type: String

    companion object : KSerializer<Action> {
        override val descriptor = JsonObject.serializer().descriptor

        override fun deserialize(decoder: Decoder): Action {
            check(decoder is JsonDecoder) {
                "Only ${JsonDecoder::class.simpleName} is supported for ${Action::class.simpleName}"
            }

            val jsonObject = decoder.decodeJsonElement()

            check(jsonObject is JsonObject) {
                "Failed to deserialize ${Action::class.simpleName}, ${JsonObject::class.simpleName} expected"
            }

            val type = jsonObject[Constants.TYPE]?.asStringOrNull()

            checkNotNull(type) {
                "Unable to create ${Action::class.simpleName} without type"
            }

            return ActionsRegistry.getAction(type)?.invoke(jsonObject, JsonNull) ?: run {
                SimpleAction(type)
            }
        }

        override fun serialize(
            encoder: Encoder,
            value: Action,
        ) {
            throw SerializationException("${Action::class.simpleName} is not serializable")
        }
    }
}
