package com.iktwo.kodices.actions

import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONObjectOrNull
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject

@Serializable
data class MessageAction(
    val text: String,
    val style: MessageStyle,
) : Action {
    override val type = TYPE

    companion object {
        const val TYPE = "message"

        val descriptor = object : ActionDescriptor {
            override val type = TYPE

            // TODO: add support for actions (a button in the message)
            override val builder: ActionBuilder = { action, _ ->
                val text = action.jsonObject[Constants.CONSTANTS]
                    ?.asJSONObjectOrNull()
                    ?.get(Constants.TEXT_KEY)
                    ?.asStringOrNull()

                val style = MessageStyle.fromString(action.jsonObject[Constants.STYLE]?.asStringOrNull())

                if (text != null) {
                    MessageAction(text, style)
                } else {
                    throw Exception("MessageDialogAction couldn't be created. Text is null in $action")
                }
            }
        }
    }
}

enum class MessageStyle {
    TOAST,
    SNACK_BAR,
    DIALOG,
    ;

    companion object {
        fun fromString(name: String?): MessageStyle {
            return entries.firstOrNull { style ->
                name.equals(style.name, ignoreCase = true)
            } ?: DIALOG
        }
    }
}
