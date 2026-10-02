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
    public val presentation: String = PRESENTATION_PUSH,
    public val title: String? = null,
) : Action {
    override val type: String = TYPE

    public companion object {
        public const val TYPE: String = "navigate"
        public const val PRESENTATION_PUSH: String = "push"
        public const val PRESENTATION_DIALOG: String = "dialog"
        public const val PRESENTATION_BOTTOM_SHEET: String = "bottom_sheet"
        public const val PRESENTATION_SHEET: String = "sheet"

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

                val presentation = actionJson.jsonObject[Constants.CONSTANTS]
                    ?.asJSONObjectOrNull()
                    ?.get("presentation")
                    ?.asStringOrNull()
                    ?: actionJson.jsonObject["presentation"]?.asStringOrNull()
                    ?: PRESENTATION_PUSH

                val title = actionJson.jsonObject[Constants.CONSTANTS]
                    ?.asJSONObjectOrNull()
                    ?.get("title")
                    ?.asStringOrNull()
                    ?: actionJson.jsonObject["title"]?.asStringOrNull()

                NavigateAction(
                    targetRoute = targetRoute,
                    presentation = presentation,
                    title = title,
                )
            }
        }
    }
}
