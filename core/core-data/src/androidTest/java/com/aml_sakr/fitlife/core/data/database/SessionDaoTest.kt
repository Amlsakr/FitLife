package com.aml_sakr.fitlife.core.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {
    private lateinit var database: WorkoutPlanDatabase
    private lateinit var dao: SessionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WorkoutPlanDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        dao = database.sessionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getSessionCountInRange_returnsCorrectCount() = runBlocking {
        val userId = "user1"
        dao.insertSession(createSession("s1", userId, 1000L))
        dao.insertSession(createSession("s2", userId, 2000L))
        dao.insertSession(createSession("s3", userId, 500L)) // Out of range
        dao.insertSession(createSession("s4", "user2", 1500L)) // Wrong user

        val count = dao.getSessionCountInRange(userId, 1000L)
        assertEquals(2, count)
    }

    @Test
    fun getTotalRepsInRange_returnsCorrectSum() = runBlocking {
        val userId = "user1"
        dao.insertSession(createSession("s1", userId, 1000L, reps = 10))
        dao.insertSession(createSession("s2", userId, 2000L, reps = 20))
        dao.insertSession(createSession("s3", userId, 500L, reps = 30)) // Out of range

        val totalReps = dao.getTotalRepsInRange(userId, 1000L)
        assertEquals(30, totalReps)
    }

    @Test
    fun getTotalFatigueEventsInRange_returnsCorrectSum() = runBlocking {
        val userId = "user1"
        dao.insertSession(createSession("s1", userId, 1000L, fatigue = 1))
        dao.insertSession(createSession("s2", userId, 2000L, fatigue = 3))

        val totalFatigue = dao.getTotalFatigueEventsInRange(userId, 1000L)
        assertEquals(4, totalFatigue)
    }

    @Test
    fun getTotalDurationInRange_returnsCorrectSum() = runBlocking {
        val userId = "user1"
        dao.insertSession(createSession("s1", userId, 1000L, duration = 300))
        dao.insertSession(createSession("s2", userId, 2000L, duration = 600))

        val totalDuration = dao.getTotalDurationInRange(userId, 1000L)
        assertEquals(900, totalDuration)
    }

    private fun createSession(
        id: String,
        userId: String,
        startTime: Long,
        reps: Int = 0,
        fatigue: Int = 0,
        duration: Int = 0
    ) = SessionEntity(
        sessionId = id,
        userId = userId,
        planId = "plan1",
        workoutDayId = "day1",
        startTime = startTime,
        endTime = startTime + duration * 1000L,
        durationSeconds = duration,
        totalReps = reps,
        totalSets = 3,
        fatigueEventCount = fatigue,
        audioFallbackUsed = false,
        completionPercentage = 1.0f,
        whatsAppShared = false
    )
}
