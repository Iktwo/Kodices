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
public data class ModalAction(
    public val targetRoute: String,
    public val presentation: String = PRESENTATION_DIALOG,
    public val title: String? = null,
) : Action {
    override val type: String = TYPE

    public companion object {
        public const val TYPE: String = "modal"
        public const val PRESENTATION_DIALOG: String = "dialog"
        public const val PRESENTATION_BOTTOM_SHEET: String = "bottom_sheet"

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
                    ?: actionJson.jsonObject["route"]?.asStringOrNull()
                    ?: ""

                val presentation = actionJson.jsonObject[Constants.CONSTANTS]
                    ?.asJSONObjectOrNull()
                    ?.get("presentation")
                    ?.asStringOrNull()
                    ?: actionJson.jsonObject["presentation"]?.asStringOrNull()
                    ?: PRESENTATION_DIALOG

                val title = actionJson.jsonObject[Constants.CONSTANTS]
                    ?.asJSONObjectOrNull()
                    ?.get("title")
                    ?.asStringOrNull()
                    ?: actionJson.jsonObject["title"]?.asStringOrNull()

                ModalAction(
                    targetRoute = targetRoute,
                    presentation = presentation,
                    title = title,
                )
            }
        }
    }
}

@Serializable
public data class DismissModalAction(
    public val target: String? = null,
) : Action {
    override val type: String = TYPE

    public companion object {
        public const val TYPE: String = "dismiss_modal"

        public val descriptor: ActionDescriptor = object : ActionDescriptor {
            override val type: String = TYPE

            override val builder: ActionBuilder = { actionJson, _ ->
                val target = actionJson.jsonObject[Constants.CONSTANTS]
                    ?.asJSONObjectOrNull()
                    ?.get("target")
                    ?.asStringOrNull()
                    ?: actionJson.jsonObject["target"]?.asStringOrNull()

                DismissModalAction(target = target)
            }
        }
    }
}
