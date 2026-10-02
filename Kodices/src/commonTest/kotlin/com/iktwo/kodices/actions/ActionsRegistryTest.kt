package com.iktwo.kodices.actions

import com.iktwo.kodices.utils.asIntOrNull
import com.iktwo.kodices.utils.asStringOrNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ActionsRegistryTest {
    @BeforeTest
    fun setup() {
        ActionsRegistry.addAction(
            object : ActionDescriptor {
                override val type = "sample"

                override val builder: ActionBuilder = { _, _ -> SimpleAction(type) }
            },
        )

        ActionsRegistry.addActions(
            listOf(
                object : ActionDescriptor {
                    override val type = "action1"

                    override val builder: ActionBuilder = { _, _ -> SimpleAction(type) }
                },
                object : ActionDescriptor {
                    override val type = "action2"

                    override val builder: ActionBuilder = { _, _ -> SimpleAction(type) }
                },
            ),
        )
    }

    @Test
    fun testActionsRegistryWithUnknownAction() {
        assertNull(ActionsRegistry.getAction("unknown"))
    }

    @Test
    fun testActionsRegistryWithKnownAction() {
        assertNotNull(ActionsRegistry.getAction("sample"))
        assertNotNull(ActionsRegistry.getAction("action1"))
        assertNotNull(ActionsRegistry.getAction("action2"))
    }

    @Test
    fun testSimpleActionRetainsJsonValuesAndEquality() {
        val parser = com.iktwo.kodices.KodicesParser()
        val content = parser.parseJSONToContent(
            """
            {
              "elements": [
                {
                  "type": "button",
                  "action": {
                    "type": "custom_unregistered",
                    "targetRoute": "detail_screen",
                    "id": 42
                  }
                }
              ]
            }
            """.trimIndent(),
        )

        assertNotNull(content)
        val action = content.elements.first().actions.first()
        kotlin.test.assertTrue(action is SimpleAction)
        kotlin.test.assertEquals("custom_unregistered", action.type)
        kotlin.test.assertEquals(
            "detail_screen",
            action.jsonValues["targetRoute"]?.asStringOrNull(),
        )
        kotlin.test.assertEquals(
            42,
            action.jsonValues["id"]?.asIntOrNull(),
        )

        val action2 = SimpleAction("custom_unregistered", action.jsonValues)
        kotlin.test.assertEquals(action, action2)
        kotlin.test.assertEquals(action.hashCode(), action2.hashCode())
    }
}
