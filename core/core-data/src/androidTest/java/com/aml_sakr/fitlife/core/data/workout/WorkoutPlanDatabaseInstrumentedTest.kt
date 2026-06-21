package com.aml_sakr.fitlife.core.data.workout

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutPlanDatabaseInstrumentedTest {
    private lateinit var database: WorkoutPlanDatabase
    private lateinit var dao: WorkoutPlanDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WorkoutPlanDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        dao = database.workoutPlanDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun roundTripsListColumnsLosslesslyThroughRoom() = runBlocking {
        val entity = sampleEntity(
            planId = "lossless",
            goalNames = listOf("Strength", "", "General${'\u001f'}Health"),
            equipmentNames = listOf("bodyweight", "band${'\u001f'}strap", " ")
        )

        dao.insert(entity)

        val restored = dao.getLatestByRequestKey(
            requestKey = entity.requestKey,
            nowEpochMillis = 0L
        )

        assertEquals(entity, restored)
    }

    @Test
    fun returnsFreshestUnexpiredPlanForTheSameRequestKey() = runBlocking {
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
    fun returnsFreshestUnexpiredPlanForTheSameUser() = runBlocking {
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
    fun clearOldRemovesOnlyExpiredRows() = runBlocking {
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
        expiresAtEpochMillis: Long = 5_000L,
        goalNames: List<String> = listOf("Strength"),
        equipmentNames: List<String> = listOf("bodyweight")
    ): WorkoutPlanEntity = WorkoutPlanEntity(
        planId = planId,
        userId = userId,
        requestKey = requestKey,
        generatedAtEpochMillis = generatedAtEpochMillis,
        expiresAtEpochMillis = expiresAtEpochMillis,
        fitnessLevel = "Beginner",
        location = "Home",
        requestedDays = 7,
        goalNames = goalNames,
        equipmentNames = equipmentNames,
        weekNumber = 1,
        isFallback = false,
        planJson = """{"days":[],"metadata":"sample"}"""
    )
}
