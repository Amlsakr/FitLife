package com.aml_sakr.fitlife.feature.progress.domain.usecase

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.usecase.CalculateCaloriesUseCase
import com.aml_sakr.fitlife.feature.progress.domain.model.ChartData
import com.aml_sakr.fitlife.feature.progress.domain.model.ChartDataPoint
import com.aml_sakr.fitlife.feature.progress.domain.repository.IProgressRepository
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetProgressChartDataUseCase @Inject constructor(
    private val repository: IProgressRepository,
    private val calculateCaloriesUseCase: CalculateCaloriesUseCase
) {
    suspend fun getWeeklySessionsTrend(userId: String, now: Long = System.currentTimeMillis()): Result<ChartData, DomainError> {
        val currentZdt = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault())
        val startOfThisWeek = currentZdt.with(java.time.DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS)
        val startOf4WeeksAgo = startOfThisWeek.minusWeeks(3)

        val result = repository.getSessionsSince(userId, startOf4WeeksAgo.toInstant().toEpochMilli())
        if (result is Result.Failure) return result
        
        val sessions = (result as Result.Success).data
        val dataPoints = mutableListOf<ChartDataPoint>()
        
        for (i in 3 downTo 0) {
            val weekStart = startOfThisWeek.minusWeeks(i.toLong())
            val weekEnd = weekStart.plusDays(7)
            
            val weekStartMilli = weekStart.toInstant().toEpochMilli()
            val weekEndMilli = weekEnd.toInstant().toEpochMilli()
            
            val count = sessions.count { it.startTime in weekStartMilli until weekEndMilli }
            
            val label = if (i == 0) "WEEK_0" else "WEEK_$i"
            dataPoints.add(ChartDataPoint(label, count.toFloat()))
        }

        return Result.Success(
            ChartData(
                dataPoints = dataPoints,
                yAxisLabel = "LABEL_SESSIONS",
                emptyMessage = "EMPTY_SESSIONS"
            )
        )
    }

    suspend fun getDailyCaloriesTrend(
        userId: String, 
        now: Long = System.currentTimeMillis(),
        weightKg: Double = CalculateCaloriesUseCase.DEFAULT_WEIGHT_KG
    ): Result<ChartData, DomainError> {
        val currentZdt = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault())
        val startOfThisWeek = currentZdt.with(java.time.DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS)
        
        val result = repository.getSessionsSince(userId, startOfThisWeek.toInstant().toEpochMilli())
        if (result is Result.Failure) return result
        
        val sessions = (result as Result.Success).data
        val dataPoints = mutableListOf<ChartDataPoint>()
        
        // Keys mapped in UI
        val days = listOf("DAY_0", "DAY_1", "DAY_2", "DAY_3", "DAY_4", "DAY_5", "DAY_6")
        
        for (i in 0..6) {
            val dayStart = startOfThisWeek.plusDays(i.toLong())
            val dayEnd = dayStart.plusDays(1)
            
            val dayStartMilli = dayStart.toInstant().toEpochMilli()
            val dayEndMilli = dayEnd.toInstant().toEpochMilli()
            
            val daySessions = sessions.filter { it.startTime in dayStartMilli until dayEndMilli }
            val durationSeconds = daySessions.sumOf { it.durationSeconds ?: 0 }
            
            val calories = if (durationSeconds > 0) calculateCaloriesUseCase(durationSeconds, weightKg = weightKg) else 0
            
            dataPoints.add(ChartDataPoint(days[i], calories.toFloat()))
        }

        return Result.Success(
            ChartData(
                dataPoints = dataPoints,
                yAxisLabel = "LABEL_CALORIES",
                emptyMessage = "EMPTY_CALORIES"
            )
        )
    }
}
