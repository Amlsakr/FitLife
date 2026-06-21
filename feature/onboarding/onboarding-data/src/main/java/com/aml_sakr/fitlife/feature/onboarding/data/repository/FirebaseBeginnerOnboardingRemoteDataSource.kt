package com.aml_sakr.fitlife.feature.onboarding.data.repository

import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseBeginnerOnboardingRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : BeginnerOnboardingRemoteDataSource {
    override suspend fun upsertBeginnerProfile(userId: String, draft: BeginnerOnboardingDraft) {
        firestore.collection(FirestoreCollections.USERS)
            .document(userId)
            .set(draft.toFirestorePayload(userId), SetOptions.merge())
            .await()
    }

    private fun BeginnerOnboardingDraft.toFirestorePayload(userId: String): Map<String, Any?> =
        mapOf(
            DocumentFields.ID to userId,
            DocumentFields.FITNESS_LEVEL to "Beginner",
            DocumentFields.GOALS to goals.map { it.name },
            DocumentFields.EQUIPMENT to equipment.map { it.name },
            DocumentFields.WEEKLY_FREQUENCY to weeklyFrequency,
            DocumentFields.BEGINNER_STEP to currentStep.name
        )

    private object FirestoreCollections {
        const val USERS = "users"
    }

    private object DocumentFields {
        const val ID = "id"
        const val FITNESS_LEVEL = "fitnessLevel"
        const val GOALS = "goals"
        const val EQUIPMENT = "equipment"
        const val WEEKLY_FREQUENCY = "weeklyFrequency"
        const val BEGINNER_STEP = "beginnerStep"
    }
}
