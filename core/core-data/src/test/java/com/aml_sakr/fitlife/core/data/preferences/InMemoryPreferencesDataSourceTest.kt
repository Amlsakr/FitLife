package com.aml_sakr.fitlife.core.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryPreferencesDataSourceTest {
    @Test
    fun inMemoryPreferencesDataSourcePersistsBooleanStringAndLongValues() = runTest {
        val dataSource = InMemoryPreferencesDataSource()

        dataSource.putBoolean("onboarding_complete", true)
        dataSource.putString("fitness_level", "BEGINNER")
        dataSource.putLong("last_sync", 42L)

        assertEquals(true, dataSource.booleanFlow("onboarding_complete", false).first())
        assertEquals("BEGINNER", dataSource.stringFlow("fitness_level", "").first())
        assertEquals(42L, dataSource.longFlow("last_sync", 0L).first())
    }
}
