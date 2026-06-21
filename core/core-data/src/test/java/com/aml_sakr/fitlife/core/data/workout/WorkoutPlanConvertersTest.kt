package com.aml_sakr.fitlife.core.data.workout

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutPlanConvertersTest {
    private val converters = WorkoutPlanConverters()

    @Test
    fun `round trips string lists losslessly`() {
        val values = listOf("Strength", "", "General${'\u001f'}Health", " ")

        val restored = converters.toStringList(converters.fromStringList(values))

        assertEquals(values, restored)
    }

    @Test
    fun `decodes legacy separator encoded rows`() {
        val restored = converters.toStringList("bodyweight${'\u001f'}chair")

        assertEquals(listOf("bodyweight", "chair"), restored)
    }

    @Test
    fun `treats blank stored values as empty lists`() {
        assertEquals(emptyList<String>(), converters.toStringList(""))
    }
}
