package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutPlanFirestoreClient(
    firestore: FirebaseFirestore
) : FirestoreRemoteSyncClient<WorkoutPlanEntity>(firestore, "workout_plans") {

    override fun mapSnapshotToEntity(snapshot: DocumentSnapshot): WorkoutPlanEntity? {
        val lastModified = snapshot.getTimestampMillis("lastModified")
            ?: snapshot.getTimestampMillis("serverUpdatedAt")
            ?: 0L

        return WorkoutPlanEntity(
            planId = snapshot.id,
            userId = snapshot.getString("userId") ?: "",
            requestKey = snapshot.getString("requestKey") ?: "",
            generatedAtEpochMillis = snapshot.getLong("generatedAtEpochMillis") ?: 0L,
            expiresAtEpochMillis = snapshot.getLong("expiresAtEpochMillis") ?: 0L,
            fitnessLevel = snapshot.getString("fitnessLevel") ?: "",
            location = snapshot.getString("location") ?: "",
            requestedDays = snapshot.getLong("requestedDays")?.toInt() ?: 0,
            goalNames = snapshot.get("goalNames") as? List<String> ?: emptyList(),
            equipmentNames = snapshot.get("equipmentNames") as? List<String> ?: emptyList(),
            weekNumber = snapshot.getLong("weekNumber")?.toInt() ?: 0,
            isFallback = snapshot.getBoolean("isFallback") ?: false,
            planJson = snapshot.getString("planJson") ?: "",
            syncStatus = SyncStatus.SYNCED,
            lastModified = lastModified
        )
    }

    override fun mapEntityToMap(entity: WorkoutPlanEntity): Map<String, Any?> = mapOf(
        "userId" to entity.userId,
        "requestKey" to entity.requestKey,
        "generatedAtEpochMillis" to entity.generatedAtEpochMillis,
        "expiresAtEpochMillis" to entity.expiresAtEpochMillis,
        "fitnessLevel" to entity.fitnessLevel,
        "location" to entity.location,
        "requestedDays" to entity.requestedDays,
        "goalNames" to entity.goalNames,
        "equipmentNames" to entity.equipmentNames,
        "weekNumber" to entity.weekNumber,
        "isFallback" to entity.isFallback,
        "planJson" to entity.planJson,
        "lastModified" to entity.lastModified
    )
}
