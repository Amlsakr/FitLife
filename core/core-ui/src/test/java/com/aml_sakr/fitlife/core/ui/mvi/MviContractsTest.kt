package com.aml_sakr.fitlife.core.ui.mvi

import org.junit.Assert.assertTrue
import org.junit.Test

class MviContractsTest {
    @Test
    fun eventAliasPreservesExistingUiEventCompatibility() {
        val event: Any = TestEvent.Submitted

        assertTrue(event is Event)
        assertTrue(event is UIEvent)
    }

    private sealed interface TestEvent : Event {
        data object Submitted : TestEvent
    }
}
