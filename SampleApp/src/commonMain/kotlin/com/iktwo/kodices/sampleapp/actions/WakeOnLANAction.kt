package com.iktwo.kodices.sampleapp.actions

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionBuilder
import com.iktwo.kodices.actions.ActionDescriptor
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONObjectOrNull
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.buildJsonObject

// TODO: there should be some type of "FormAction" which
//  gathers data from form and then that can be used as source to another action,
//  which would receive that data as a parameter on the builder.
//  That means an action should not be resolved (built) until it will be executed
class WakeOnLANAction(
    val ipFieldName: String,
    val macFieldName: String,
    val portFieldName: String,
) : Action {
    override val type = TYPE

    companion object : ActionDescriptor {
        const val TYPE = "WakeOnLAN"
        private const val FIELD_IP = "ip"
        private const val FIELD_MAC = "mac"
        private const val FIELD_PORT = "port"

        override val type = TYPE
        override val builder: ActionBuilder = { jsonAction, _ ->
            val constants = jsonAction.asJSONObjectOrNull()?.get(Constants.CONSTANTS)?.asJSONObjectOrNull() ?: buildJsonObject { }
            WakeOnLANAction(
                ipFieldName = constants[FIELD_IP]?.asStringOrNull() ?: "",
                macFieldName = constants[FIELD_MAC]?.asStringOrNull() ?: "",
                portFieldName = constants[FIELD_PORT]?.asStringOrNull() ?: "",
            )
        }
    }
}
