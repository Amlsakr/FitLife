package com.aml_sakr.fitlife.core.data.workout

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutPlanDaoSemanticsTest {
    private val dao = InMemoryWorkoutPlanDao()

    @Test
    fun `returns freshest unexpired plan for the same request key`() = runTest {
        dao.insert(sampleEntity(planId = "older", generatedAtEpochMillis = 1_000L, expiresAtEpochMillis = 10_000L))
        dao.insert(sampleEntity(planId = "newer", generatedAtEpochMillis = 2_000L, expiresAtEpochMillis = 10_000L))
        dao.insert(sampleEntity(planId = "expired", generatedAtEpochMillis = 3_000L, expiresAtEpochMillis = 500L))
        dao.insert(sampleEntity(planId = "mismatch", requestKey = "other-request", generatedAtEpochMillis = 4_000L))

        val restored = dao.getLatestByRequestKey(
            requestKey = "request-a",
            nowEpochMillis = 1_500L
        )

        assertEquals("newer", restored?.planId)
    }

    @Test
    fun `returns freshest unexpired plan for the same user`() = runTest {
        dao.insert(sampleEntity(planId = "first", userId = "user-a", generatedAtEpochMillis = 1_000L, expiresAtEpochMillis = 10_000L))
        dao.insert(sampleEntity(planId = "second", userId = "user-a", generatedAtEpochMillis = 3_000L, expiresAtEpochMillis = 10_000L))
        dao.insert(sampleEntity(planId = "wrong-request", userId = "user-a", requestKey = "other-request", generatedAtEpochMillis = 4_000L, expiresAtEpochMillis = 10_000L))
        dao.insert(sampleEntity(planId = "expired", userId = "user-a", generatedAtEpochMillis = 4_000L, expiresAtEpochMillis = 500L))
        dao.insert(sampleEntity(planId = "other-user", userId = "user-b", generatedAtEpochMillis = 5_000L))

        val restored = dao.getLatestByUserIdAndRequestKey(
            userId = "user-a",
            requestKey = "request-a",
            nowEpochMillis = 2_000L
        )

        assertEquals("second", restored?.planId)
    }

    @Test
    fun `clear old removes only expired rows`() = runTest {
        dao.insert(sampleEntity(planId = "fresh", expiresAtEpochMillis = 10_000L))
        dao.insert(sampleEntity(planId = "expired", expiresAtEpochMillis = 500L))

        val removedCount = dao.clearOld(nowEpochMillis = 1_000L)

        assertEquals(1, removedCount)
        assertEquals("fresh", dao.getLatestByRequestKey("request-a", nowEpochMillis = 1_000L)?.planId)
        assertNull(dao.getLatestByRequestKey("request-a", nowEpochMillis = 11_000L))
    }

    private fun sampleEntity(
        planId: String,
        userId: String = "user-a",
        requestKey: String = "request-a",
        generatedAtEpochMillis: Long = 1_000L,
        expiresAtEpochMillis: Long = 5_000L
    ): WorkoutPlanEntity = WorkoutPlanEntity(
        planId = planId,
        userId = userId,
        requestKey = requestKey,
        generatedAtEpochMillis = generatedAtEpochMillis,
        expiresAtEpochMillis = expiresAtEpochMillis,
        fitnessLevel = "Beginner",
        location = "Home",
        requestedDays = 7,
        goalNames = listOf("Strength"),
        equipmentNames = listOf("bodyweight"),
        weekNumber = 1,
        isFallback = false,
        planJson = """{"days":[],"metadata":"sample"}"""
    )
}

private class InMemoryWorkoutPlanDao : WorkoutPlanDao {
    private val rows = linkedMapOf<String, WorkoutPlanEntity>()

    override suspend fun insert(entity: WorkoutPlanEntity) {
        rows[entity.planId] = entity
    }

    override suspend fun getLatestByRequestKey(
        requestKey: String,
        nowEpochMillis: Long
    ): WorkoutPlanEntity? =
        rows.values
            .asSequence()
            .filter { it.requestKey == requestKey && it.expiresAtEpochMillis > nowEpochMillis }
            .sortedByDescending { it.generatedAtEpochMillis }
            .firstOrNull()

    override suspend fun getLatestByUserIdAndRequestKey(
        userId: String,
        requestKey: String,
        nowEpochMillis: Long
    ): WorkoutPlanEntity? =
        rows.values
            .asSequence()
            .filter {
                it.userId == userId &&
                    it.requestKey == requestKey &&
                    it.expiresAtEpochMillis > nowEpochMillis
            }
            .sortedByDescending { it.generatedAtEpochMillis }
            .firstOrNull()

    override suspend fun clearOld(nowEpochMillis: Long): Int {
        val toRemove = rows.values
            .filter { it.expiresAtEpochMillis <= nowEpochMillis }
            .map { it.planId }
        toRemove.forEach { rows.remove(it) }
        return toRemove.size
    }
}
