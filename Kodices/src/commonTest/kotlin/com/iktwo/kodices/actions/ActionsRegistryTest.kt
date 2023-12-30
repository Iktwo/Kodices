package com.iktwo.kodices.actions

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
}
