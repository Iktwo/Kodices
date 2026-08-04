package com.iktwo.piktographs.navigation

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionBuilder
import com.iktwo.kodices.actions.ActionDescriptor
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONObjectOrNull
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject

@Serializable
public data class NavigateAction(
    public val targetRoute: String,
) : Action {
    override val type: String = TYPE

    public companion object {
        public const val TYPE: String = "navigate"

        public val descriptor: ActionDescriptor = object : ActionDescriptor {
            override val type: String = TYPE

            override val builder: ActionBuilder = { actionJson, _ ->
                val targetRoute = actionJson.jsonObject[Constants.CONSTANTS]
                    ?.asJSONObjectOrNull()
                    ?.get("targetRoute")
                    ?.asStringOrNull()
                    ?: actionJson.jsonObject["targetRoute"]?.asStringOrNull()
                    ?: actionJson.jsonObject["target"]?.asStringOrNull()
                    ?: actionJson.jsonObject[Constants.CONSTANTS]
                        ?.asJSONObjectOrNull()
                        ?.get("target")
                        ?.asStringOrNull()
                    ?: ""

                NavigateAction(targetRoute)
            }
        }
    }
}
