package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.database.SessionEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class SessionFirestoreClient(
    firestore: FirebaseFirestore
) : FirestoreRemoteSyncClient<SessionEntity>(firestore, "sessions") {

    override fun mapSnapshotToEntity(snapshot: DocumentSnapshot): SessionEntity? {
        val lastModified = snapshot.getTimestampMillis("lastModified")
            ?: snapshot.getTimestampMillis("serverUpdatedAt")
            ?: 0L

        return SessionEntity(
            sessionId = snapshot.id,
            userId = snapshot.getString("userId") ?: "",
            planId = snapshot.getString("planId") ?: "",
            workoutDayId = snapshot.getString("workoutDayId") ?: "",
            startTime = snapshot.getLong("startTime") ?: 0L,
            endTime = snapshot.getLong("endTime"),
            durationSeconds = snapshot.getLong("durationSeconds")?.toInt(),
            totalReps = snapshot.getLong("totalReps")?.toInt() ?: 0,
            totalSets = snapshot.getLong("totalSets")?.toInt() ?: 0,
            fatigueEventCount = snapshot.getLong("fatigueEventCount")?.toInt() ?: 0,
            audioFallbackUsed = snapshot.getBoolean("audioFallbackUsed") ?: false,
            completionPercentage = snapshot.getDouble("completionPercentage")?.toFloat() ?: 0f,
            whatsAppShared = snapshot.getBoolean("whatsAppShared") ?: false,
            syncStatus = SyncStatus.SYNCED,
            lastModified = lastModified
        )
    }

    override fun mapEntityToMap(entity: SessionEntity): Map<String, Any?> = mapOf(
        "userId" to entity.userId,
        "planId" to entity.planId,
        "workoutDayId" to entity.workoutDayId,
        "startTime" to entity.startTime,
        "endTime" to entity.endTime,
        "durationSeconds" to entity.durationSeconds,
        "totalReps" to entity.totalReps,
        "totalSets" to entity.totalSets,
        "fatigueEventCount" to entity.fatigueEventCount,
        "audioFallbackUsed" to entity.audioFallbackUsed,
        "completionPercentage" to entity.completionPercentage,
        "whatsAppShared" to entity.whatsAppShared,
        "lastModified" to entity.lastModified
    )
}
