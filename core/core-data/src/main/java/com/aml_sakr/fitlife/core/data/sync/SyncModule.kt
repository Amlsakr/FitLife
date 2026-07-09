package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
import com.aml_sakr.fitlife.core.data.database.SessionDao
import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().also { firestore ->
        val shouldUseEmulator = System.getProperty("fitlife.firestore.useEmulator")?.toBoolean()
            ?: false
        if (shouldUseEmulator) {
            firestore.useEmulator("10.0.2.2", 8080)
        }
    }

    @Provides
    @Singleton
    fun provideOfflineSyncCoordinator(
        workoutPlanDao: WorkoutPlanDao,
        sessionDao: SessionDao,
        firestore: FirebaseFirestore,
        connectivityMonitor: ConnectivityMonitor
    ): OfflineSyncCoordinator {
        val agents = listOf(
            DefaultSyncAgent(workoutPlanDao, WorkoutPlanFirestoreClient(firestore)),
            DefaultSyncAgent(sessionDao, SessionFirestoreClient(firestore))
        )
        return OfflineSyncCoordinator(agents, connectivityMonitor)
    }
}
