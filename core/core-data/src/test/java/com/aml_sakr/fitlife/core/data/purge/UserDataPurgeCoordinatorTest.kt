package com.aml_sakr.fitlife.core.data.purge

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserDataPurgeCoordinatorTest {
    @Test
    fun purgeUserData_invokesContributorsInOrder() = runTest {
        val calls = mutableListOf<String>()
        val coordinator = UserDataPurgeCoordinator(
            setOf(
                RecordingContributor("first", calls),
                RecordingContributor("second", calls)
            )
        )

        coordinator.purgeUserData("user-1")

        assertEquals(listOf("first:user-1", "second:user-1"), calls)
    }

    private class RecordingContributor(
        private val name: String,
        private val calls: MutableList<String>
    ) : UserDataPurgeContributor {
        override suspend fun purgeUserData(userId: String) {
            calls += "$name:$userId"
        }
    }
}
