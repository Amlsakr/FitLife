package com.aml_sakr.fitlife.feature.onboarding.data.repository

import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseIntermediateOnboardingRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : IntermediateOnboardingRemoteDataSource {
    override suspend fun upsertIntermediateProfile(userId: String, draft: IntermediateOnboardingDraft) {
        firestore.collection(FirestoreCollections.USERS)
            .document(userId)
            .set(draft.toFirestorePayload(userId), SetOptions.merge())
            .await()
    }

    private fun IntermediateOnboardingDraft.toFirestorePayload(
        userId: String
    ): Map<String, Any?> = buildMap {
        put(DocumentFields.ID, userId)
        put(DocumentFields.FITNESS_LEVEL, "Intermediate")
        put(DocumentFields.CURRENT_SPLIT, currentSplit?.name)
        put(DocumentFields.GOALS, goals.map { it.name })
        put(
            DocumentFields.ONE_REP_MAX,
            oneRepMax.entries.associate { (lift, max) -> lift.name to max }
        )
        put(DocumentFields.INTERMEDIATE_STEP, currentStep.name)
    }

    private object FirestoreCollections {
        const val USERS = "users"
    }

    private object DocumentFields {
        const val ID = "id"
        const val FITNESS_LEVEL = "fitnessLevel"
        const val CURRENT_SPLIT = "currentSplit"
        const val GOALS = "goals"
        const val ONE_REP_MAX = "oneRepMax"
        const val INTERMEDIATE_STEP = "intermediateStep"
    }
}
